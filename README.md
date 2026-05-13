# Silver Price Bot

Sends silver price to Telegram every hour. Supports 3 implementations and multiple data sources.

## Project Structure

```
silver-price-bot/
├── src/        # Java implementation (Spring Boot) — port 8081
├── python/     # Python implementation (Flask) — port 8080, 8082
└── go/         # Go implementation — port 8083
```

See implementation-specific READMEs:
- [python/README.md](python/README.md)
- [go/README.md](go/README.md)

## Data Sources

| Source | Data |
|---|---|
| goldprice.org | XAG/USD spot price |
| muavangbac.vn | Phu Quy silver piece & bar (VND) |
| giabac.vn | Silver 999 per chi/luong/kg (VND) |

## Environment Variables

```bash
export TELEGRAM_BOT_TOKEN="your_bot_token"
export TELEGRAM_CHAT_ID="your_chat_id"
export DISCORD_WEBHOOK_URL="your_discord_webhook_url"  # optional, Java only
export TIMEZONE="Asia/Ho_Chi_Minh"                     # optional, default UTC
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
  -e TIMEZONE=$TIMEZONE \
  -p 8081:8081 \
  --name silver-bot-java \
  --restart unless-stopped \
  silver-bot-java
```

**Manual trigger:**

```bash
curl -X POST http://localhost:8081/api/price/trigger
```
