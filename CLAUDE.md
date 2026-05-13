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

Before running, set real values in [src/main/resources/application.properties](src/main/resources/application.properties):

```
telegram.bot.token=<bot token from @BotFather>
telegram.chat.id=<target chat or group ID>
```

## Architecture

This is a Spring Boot 3.2.5 / Java 21 scheduled bot with no web endpoints exposed. The entire runtime loop is:

1. **`PriceScheduler`** fires every hour (`fixedRate = 3_600_000 ms`) via `@Scheduled`.
2. It calls **`SilverPriceService.fetchSilverPrice()`**, which hits the goldprice.org internal API (`https://data-asg.goldprice.org/GetData/USD/1`) with spoofed `User-Agent`/`Referer` headers and parses the JSON array response — index 1 is the XAG (silver) USD price, index 2 is change%. A legacy colon-delimited fallback is also handled.
3. The resulting **`SilverPrice`** model formats a Vietnamese-language Markdown message via `toTelegramMessage()`.
4. **`TelegramService.sendMessage()`** POSTs that message to the Telegram Bot API (`/sendMessage`) using `parse_mode=Markdown`.

On any exception, the scheduler catches it and sends an error message to the same Telegram chat instead of crashing.

There is no database, no REST controller, and no external state — each scheduled tick is fully stateless.
