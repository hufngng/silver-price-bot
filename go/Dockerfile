FROM golang:1.22-alpine AS builder
WORKDIR /app
COPY go.mod ./
# tidy + download inside Docker — no local go.sum needed
RUN go mod tidy || true
COPY . .
RUN go mod tidy && go mod download
RUN CGO_ENABLED=0 GOOS=linux go build -o bot ./cmd/bot

FROM alpine:3.19
RUN apk add --no-cache tzdata ca-certificates
WORKDIR /app
COPY --from=builder /app/bot .

ENV TELEGRAM_BOT_TOKEN=""
ENV TELEGRAM_CHAT_ID=""
ENV TIMEZONE=""
ENV PORT=8083

EXPOSE 8083
ENTRYPOINT ["./bot"]
