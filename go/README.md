# Silver Price Bot — Go

Go implementation running on port **8083**. Fetches from 3 sources each hour.

## Data Sources

| Fetcher | Source | Data |
|---|---|---|
| goldprice | goldprice.org | XAG/USD spot price |
| muavangbac | muavangbac.vn | Phu Quy silver piece & bar (VND) |
| giabac | giabac.vn | Silver 999 per chi/luong/kg (VND) |

## Environment Variables

```bash
export TELEGRAM_BOT_TOKEN="your_bot_token"
export TELEGRAM_CHAT_ID="your_chat_id"
export TIMEZONE="Asia/Ho_Chi_Minh"  # optional, default UTC
export PORT=8083                     # optional
```

## Run with Docker

```bash
cd go/

docker build -t silver-bot-go .

docker run -d \
  -e TELEGRAM_BOT_TOKEN=$TELEGRAM_BOT_TOKEN \
  -e TELEGRAM_CHAT_ID=$TELEGRAM_CHAT_ID \
  -e TIMEZONE=$TIMEZONE \
  -p 8083:8083 \
  --name silver-bot-go \
  --restart unless-stopped \
  silver-bot-go
```

## Run directly

```bash
cd go/
go mod tidy
go run ./cmd/bot
```

## Manual trigger

```bash
curl -X POST http://localhost:8083/api/price/trigger
```

## Project Structure

```
go/
├── cmd/bot/main.go              # entrypoint
├── internal/
│   ├── config/config.go         # load env vars
│   ├── fetcher/
│   │   ├── fetcher.go           # Fetcher interface
│   │   ├── goldprice.go         # goldprice.org JSON API
│   │   ├── muavangbac.go        # muavangbac.vn HTML scrape
│   │   └── giabac.go            # giabac.vn HTML scrape (chi/luong/kg)
│   ├── notifier/telegram.go     # Telegram sender
│   ├── scheduler/scheduler.go   # cron job
│   └── server/server.go         # HTTP trigger endpoint
├── go.mod
└── Dockerfile
```
