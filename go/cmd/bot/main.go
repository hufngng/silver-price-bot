package main

import (
	"log"

	"silver-price-bot/internal/config"
	"silver-price-bot/internal/fetcher"
	"silver-price-bot/internal/notifier"
	"silver-price-bot/internal/scheduler"
	"silver-price-bot/internal/server"
)

func main() {
	cfg := config.Load()

	telegram := notifier.NewTelegram(cfg.TelegramToken, cfg.TelegramChatID)

	var fetchers []fetcher.Fetcher
	if cfg.EnableGoldprice {
		fetchers = append(fetchers, fetcher.NewGoldprice(cfg.Location))
	}
	if cfg.EnableMuaVangBac {
		fetchers = append(fetchers, fetcher.NewMuaVangBac(cfg.Location))
	}
	if cfg.EnableGiaBac {
		fetchers = append(fetchers, fetcher.NewGiaBac(cfg.Location))
	}

	sched := scheduler.New(cfg.Location, fetchers, telegram)
	sched.Start()
	defer sched.Stop()

	log.Println("Starting silver price bot...")
	go sched.Run() // run immediately on startup

	server.New(sched).Start(":" + cfg.Port)
}
