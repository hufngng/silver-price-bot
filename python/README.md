# Silver Price Bot — Python

Flask-based bot running on port **8080** and **8082**.

## Sources

| Bot | Source | Port |
|---|---|---|
| `silver_bot.py` | goldprice.org (JSON API) | 8080 |
| `muavangbac_bot.py` | muavangbac.vn (HTML scrape) | 8082 |

## Environment Variables

```bash
export TELEGRAM_BOT_TOKEN="your_bot_token"
export TELEGRAM_CHAT_ID="your_chat_id"
export TIMEZONE="Asia/Ho_Chi_Minh"  # optional, default UTC
```

## Run directly

```bash
pip install -r requirements.txt
python silver_bot.py
# or
python muavangbac_bot.py
```

## Run with Docker

**silver_bot:**
```bash
docker build -f Dockerfile -t silver-bot-python .

docker run -d \
  -e TELEGRAM_BOT_TOKEN=$TELEGRAM_BOT_TOKEN \
  -e TELEGRAM_CHAT_ID=$TELEGRAM_CHAT_ID \
  -e TIMEZONE=$TIMEZONE \
  -p 8080:8080 \
  --name silver-bot-python \
  --restart unless-stopped \
  silver-bot-python
```

**muavangbac_bot:**
```bash
docker build -f Dockerfile.muavangbac -t muavangbac-bot .

docker run -d \
  -e TELEGRAM_BOT_TOKEN=$TELEGRAM_BOT_TOKEN \
  -e TELEGRAM_CHAT_ID=$TELEGRAM_CHAT_ID \
  -e TIMEZONE=$TIMEZONE \
  -p 8082:8082 \
  --name muavangbac-bot \
  --restart unless-stopped \
  muavangbac-bot
```

## Manual trigger

```bash
curl -X POST http://localhost:8080/api/price/trigger
curl -X POST http://localhost:8082/api/price/trigger
```
