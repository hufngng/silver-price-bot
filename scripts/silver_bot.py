import os
import threading
import requests
import schedule
import time
from datetime import datetime, timezone
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError
from flask import Flask, jsonify

API_URL = "https://data-asg.goldprice.org/dbXRates/USD"
_tz_name = os.getenv("TIMEZONE", "")
try:
    TZ = ZoneInfo(_tz_name) if _tz_name else timezone.utc
except ZoneInfoNotFoundError:
    TZ = timezone.utc


def _telegram_url() -> str:
    token = os.getenv("TELEGRAM_BOT_TOKEN", "")
    return f"https://api.telegram.org/bot{token}/sendMessage"

app = Flask(__name__)


def fetch_silver_price() -> dict:
    headers = {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36",
        "Referer": "https://goldprice.org/",
    }
    resp = requests.get(API_URL, headers=headers, timeout=10)
    resp.raise_for_status()
    item = resp.json()["items"][0]
    return {"price_usd": item["xagPrice"], "change_percent": item["pcXag"]}


def send_telegram(text: str):
    payload = {"chat_id": os.getenv("TELEGRAM_CHAT_ID", ""), "text": text, "parse_mode": "Markdown"}
    resp = requests.post(_telegram_url(), json=payload, timeout=10)
    resp.raise_for_status()


def build_message(price_usd: float, change_percent: float) -> str:
    sign = "+" if change_percent >= 0 else ""
    now = datetime.now(TZ).strftime("%d/%m/%Y %H:%M:%S")
    return (
        f"🥈 *Silver Price - USD/oz*\n"
        f"💰 Price: `${price_usd:.2f}`\n"
        f"📈 Change: `{sign}{change_percent:.2f}%`\n"
        f"🕐 Updated: `{now}`\n"
        f"🔗 Source: goldprice.org"
    )


def job():
    print(f"[{datetime.now(TZ).strftime('%Y-%m-%d %H:%M:%S')}] Fetching silver price...")
    try:
        data = fetch_silver_price()
        msg = build_message(data["price_usd"], data["change_percent"])
        send_telegram(msg)
        print(f"  → Price: ${data['price_usd']:.2f} ({data['change_percent']:+.2f}%)")
        return data
    except Exception as e:
        error_msg = f"❌ Failed to fetch silver price: {e}"
        print(f"  → {error_msg}")
        try:
            send_telegram(error_msg)
        except Exception:
            pass
        raise


@app.post("/api/price/trigger")
def trigger():
    try:
        data = job()
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


def run_scheduler():
    schedule.every().hour.at(":00").do(job)
    while True:
        schedule.run_pending()
        time.sleep(1)


if __name__ == "__main__":
    try:
        job()  # run immediately on startup
    except Exception:
        pass  # startup failure must not crash Flask
    threading.Thread(target=run_scheduler, daemon=True).start()
    app.run(host="0.0.0.0", port=8080)
