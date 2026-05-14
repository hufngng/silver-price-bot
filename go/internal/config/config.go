package config

import (
	"os"
	"time"
)

type Config struct {
	TelegramToken    string
	TelegramChatID   string
	Location         *time.Location
	Port             string
	EnableGoldprice  bool
	EnableMuaVangBac bool
	EnableGiaBac     bool
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
		TelegramToken:    os.Getenv("TELEGRAM_BOT_TOKEN"),
		TelegramChatID:   os.Getenv("TELEGRAM_CHAT_ID"),
		Location:         loc,
		Port:             port,
		EnableGoldprice:  getEnvBool("FETCHER_GOLDPRICE", true),
		EnableMuaVangBac: getEnvBool("FETCHER_MUAVANGBAC", false),
		EnableGiaBac:     getEnvBool("FETCHER_GIABAC", false),
	}
}

func getEnvBool(key string, defaultVal bool) bool {
	v := os.Getenv(key)
	if v == "false" || v == "0" {
		return false
	}
	if v == "true" || v == "1" {
		return true
	}
	return defaultVal
}
