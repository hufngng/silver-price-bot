package server

import (
	"encoding/json"
	"log"
	"net/http"
	"silver-price-bot/internal/scheduler"
)

type Server struct {
	sched *scheduler.Scheduler
}

func New(sched *scheduler.Scheduler) *Server {
	return &Server{sched: sched}
}

func (s *Server) Start(addr string) {
	mux := http.NewServeMux()
	mux.HandleFunc("POST /api/price/trigger", s.trigger)
	log.Printf("HTTP server listening on %s", addr)
	log.Fatal(http.ListenAndServe(addr, mux))
}

func (s *Server) trigger(w http.ResponseWriter, r *http.Request) {
	log.Printf("POST /api/price/trigger — manual trigger received")
	go s.sched.Run()
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
}
