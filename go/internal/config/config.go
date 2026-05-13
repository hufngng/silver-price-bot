package config

import (
	"os"
	"time"
)

type Config struct {
	TelegramToken  string
	TelegramChatID string
	Location       *time.Location
	Port           string
}

func Load() *Config {
	loc := time.UTC
	if tz := os.Getenv("TIMEZONE"); tz != "" {
		if l, err := time.LoadLocation(tz); err == nil {
			loc = l
		}
	}

	port := os.Getenv("PORT")
	if port == "" {
		port = "8083"
	}

	return &Config{
		TelegramToken:  os.Getenv("TELEGRAM_BOT_TOKEN"),
		TelegramChatID: os.Getenv("TELEGRAM_CHAT_ID"),
		Location:       loc,
		Port:           port,
	}
}
