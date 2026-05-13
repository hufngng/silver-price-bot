import os
import threading
import requests
import schedule
import time
from datetime import datetime, timezone
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError
from bs4 import BeautifulSoup
from flask import Flask, jsonify

URL = "https://muavangbac.vn/"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36",
    "Referer": "https://muavangbac.vn/",
}

_tz_name = os.getenv("TIMEZONE", "")
try:
    TZ = ZoneInfo(_tz_name) if _tz_name else timezone.utc
except ZoneInfoNotFoundError:
    TZ = timezone.utc


def _telegram_url() -> str:
    token = os.getenv("TELEGRAM_BOT_TOKEN", "")
    return f"https://api.telegram.org/bot{token}/sendMessage"


app = Flask(__name__)


def fetch_prices() -> dict:
    resp = requests.get(URL, headers=HEADERS, timeout=10)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.text, "html.parser")

    # XAG/USD: second .crypto-price span (first is gold)
    crypto_prices = soup.select("span.crypto-price")
    xag_usd = float(crypto_prices[1].text.strip().replace(",", ""))

    # Phu Quy silver: all .gold-price.d-none.d-lg-block after .silver-icon-pq sections
    pq_sections = soup.select("span.silver-icon-pq")
    pq_silver_piece = pq_sections[0].find_parent("h2").find_next_sibling("div")
    pq_silver_bar = pq_sections[1].find_parent("h2").find_next_sibling("div")

    def extract_prices(section):
        values = section.select("span.gold-price.d-none.d-lg-block")
        def parse(el):
            return int("".join(filter(str.isdigit, el.text.strip().split("đ")[0].replace(",", "").replace(".", ""))))
        return {"buy": parse(values[0]), "sell": parse(values[1])}

    return {
        "xag_usd": xag_usd,
        "silver_piece": extract_prices(pq_silver_piece),
        "silver_bar": extract_prices(pq_silver_bar),
    }


def build_message(data: dict) -> str:
    now = datetime.now(TZ).strftime("%d/%m/%Y %H:%M:%S")
    sp = data["silver_piece"]
    sb = data["silver_bar"]
    return (
        f"🥈 *Silver Price - muavangbac.vn*\n\n"
        f"🌐 *XAG/USD:* `${data['xag_usd']:.3f}`\n\n"
        f"🏷 *Phu Quy Silver Piece* _(VND/tael)_\n"
        f"  Buy:  `{sp['buy']:,}`\n"
        f"  Sell: `{sp['sell']:,}`\n\n"
        f"🏷 *Phu Quy Silver Bar 1kg* _(VND/kg)_\n"
        f"  Buy:  `{sb['buy']:,}`\n"
        f"  Sell: `{sb['sell']:,}`\n\n"
        f"🕐 `{now}`\n"
        f"🔗 muavangbac.vn"
    )


def send_telegram(text: str):
    payload = {"chat_id": os.getenv("TELEGRAM_CHAT_ID", ""), "text": text, "parse_mode": "Markdown"}
    resp = requests.post(_telegram_url(), json=payload, timeout=10)
    resp.raise_for_status()


def job():
    print(f"[{datetime.now(TZ).strftime('%Y-%m-%d %H:%M:%S')}] Job started")
    try:
        data = fetch_prices()
        msg = build_message(data)
        send_telegram(msg)
        print(f"[{datetime.now(TZ).strftime('%Y-%m-%d %H:%M:%S')}] Done — XAG/USD: ${data['xag_usd']:.3f}")
        return data
    except Exception as e:
        error_msg = f"❌ Failed to fetch muavangbac.vn: {e}"
        print(f"[{datetime.now(TZ).strftime('%Y-%m-%d %H:%M:%S')}] Error — {error_msg}")
        try:
            send_telegram(error_msg)
        except Exception:
            pass
        raise


@app.post("/api/price/trigger")
def trigger():
    start = time.time()
    print(f"[{datetime.now(TZ).strftime('%Y-%m-%d %H:%M:%S')}] POST /api/price/trigger called")
    try:
        data = job()
        elapsed = time.time() - start
        print(f"[{datetime.now(TZ).strftime('%Y-%m-%d %H:%M:%S')}] Trigger done (elapsed: {elapsed:.2f}s)")
        return jsonify(data)
    except Exception as e:
        elapsed = time.time() - start
        print(f"[{datetime.now(TZ).strftime('%Y-%m-%d %H:%M:%S')}] Trigger failed after {elapsed:.2f}s")
        return jsonify({"error": str(e)}), 500


def run_scheduler():
    schedule.every().hour.at(":00").do(job)
    while True:
        schedule.run_pending()
        time.sleep(1)


if __name__ == "__main__":
    try:
        job()
    except Exception:
        pass
    threading.Thread(target=run_scheduler, daemon=True).start()
    app.run(host="0.0.0.0", port=8082)
