package scheduler

import (
	"context"
	"fmt"
	"log"
	"time"

	"github.com/robfig/cron/v3"
	"silver-price-bot/internal/fetcher"
	"silver-price-bot/internal/notifier"
)

type Scheduler struct {
	cron     *cron.Cron
	fetchers []fetcher.Fetcher
	telegram *notifier.Telegram
	loc      *time.Location
}

func New(loc *time.Location, fetchers []fetcher.Fetcher, telegram *notifier.Telegram) *Scheduler {
	return &Scheduler{
		cron:     cron.New(cron.WithLocation(loc)),
		fetchers: fetchers,
		telegram: telegram,
		loc:      loc,
	}
}

func (s *Scheduler) Start() {
	s.cron.AddFunc("0 * * * *", s.run) // top of every hour
	s.cron.Start()
}

func (s *Scheduler) Stop() {
	s.cron.Stop()
}

// Run executes the job immediately (used for startup and manual trigger).
func (s *Scheduler) Run() {
	s.run()
}

func (s *Scheduler) run() {
	start := time.Now()
	log.Printf("[%s] Job started", start.In(s.loc).Format("2006-01-02 15:04:05"))
	ctx := context.Background()

	for _, f := range s.fetchers {
		msg, err := f.Fetch(ctx)
		if err != nil {
			errMsg := fmt.Sprintf("❌ Failed to fetch %s: %v", f.Name(), err)
			log.Printf("Error: %s", errMsg)
			_ = s.telegram.Send(errMsg)
			continue
		}
		if err := s.telegram.Send(msg); err != nil {
			log.Printf("Telegram send error (%s): %v", f.Name(), err)
		}
	}

	log.Printf("[%s] Job done (elapsed: %s)", time.Now().In(s.loc).Format("2006-01-02 15:04:05"), time.Since(start).Round(time.Millisecond))
}
