# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Build & run the fat JAR
java -jar target/silver-price-bot-1.0.0.jar
```

No test suite exists in this project.

## Configuration

Set environment variables before running:

```bash
export TELEGRAM_BOT_TOKEN=<bot token from @BotFather>
export TELEGRAM_CHAT_ID=<target chat or group ID>
export DISCORD_WEBHOOK_URL=<webhook url>  # optional
export TIMEZONE=Asia/Ho_Chi_Minh         # optional, default UTC
```

## Architecture

This is a Spring Boot 3.2.5 / Java 21 scheduled bot. The entire runtime loop is:

1. **`PriceScheduler`** fires at the top of every hour (`cron = "0 0 * * * *"`, timezone from `TIMEZONE` env var) via `@Scheduled`.
2. It calls **`SilverPriceService.fetchSilverPrice()`**, which hits `https://data-asg.goldprice.org/dbXRates/USD` with spoofed `User-Agent`/`Referer` headers and parses `items[0].xagPrice` (price) and `items[0].pcXag` (change%).
3. The resulting **`SilverPrice`** model formats a Markdown message via `toMessage()`.
4. **`NotificationRouter`** fans out to all enabled channels: **`TelegramService`** and/or **`DiscordService`**, each enabled via `@ConditionalOnProperty`. Both support splitting messages that exceed the per-platform character limit (Telegram 4096, Discord 2000) using `MessageUtil.split()`.

On any exception, the scheduler catches it and sends an error message to all channels instead of crashing.

There is no database and no external state — each scheduled tick is fully stateless. A REST endpoint `POST /api/price/trigger` exists for manual triggering.
