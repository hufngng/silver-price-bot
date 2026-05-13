# Silver Price Bot

Sends silver spot price (XAG/USD) to Telegram and/or Discord every hour, sourced from goldprice.org.

## Environment Variables

```bash
export TELEGRAM_BOT_TOKEN="your_bot_token"
export TELEGRAM_CHAT_ID="your_chat_id"
export DISCORD_WEBHOOK_URL="your_discord_webhook_url"  # optional
```

> For zsh: add the lines above to `~/.zshrc` then run `source ~/.zshrc`.

## Notification Channels

Configured in `src/main/resources/application.yml`. Enable one or both:

```yaml
notification:
  telegram:
    enabled: true
  discord:
    enabled: false
    webhook-url: ${DISCORD_WEBHOOK_URL:}
```

## Java (Spring Boot) — port 8081

**Run directly:**

```bash
mvn spring-boot:run
```

**Run with Docker:**

```bash
docker build -t silver-bot-java .

docker run -d \
  -e TELEGRAM_BOT_TOKEN=$TELEGRAM_BOT_TOKEN \
  -e TELEGRAM_CHAT_ID=$TELEGRAM_CHAT_ID \
  -p 8081:8081 \
  --name silver-bot-java \
  --restart unless-stopped \
  silver-bot-java
```

**Manual trigger:**

```bash
curl -X POST http://localhost:8081/api/price/trigger
```

## Python — port 8080

**Run directly:**

```bash
cd scripts
pip install -r requirements.txt
python silver_bot.py
```

**Run with Docker:**

```bash
cd scripts

docker build -t silver-bot-python .

docker run -d \
  -e TELEGRAM_BOT_TOKEN=$TELEGRAM_BOT_TOKEN \
  -e TELEGRAM_CHAT_ID=$TELEGRAM_CHAT_ID \
  -p 8080:8080 \
  --name silver-bot-python \
  --restart unless-stopped \
  silver-bot-python
```

**Manual trigger:**

```bash
curl -X POST http://localhost:8080/api/price/trigger
```
