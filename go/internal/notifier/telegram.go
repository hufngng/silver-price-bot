package notifier

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"time"
)

const (
	telegramAPI = "https://api.telegram.org/bot%s/sendMessage"
	maxLength   = 4096
	maxRetries  = 3
	retryDelay  = 3 * time.Second
)

type Telegram struct {
	token  string
	chatID string
	client *http.Client
}

func NewTelegram(token, chatID string) *Telegram {
	return &Telegram{
		token:  token,
		chatID: chatID,
		client: &http.Client{Timeout: 10 * time.Second},
	}
}

func (t *Telegram) Send(message string) error {
	for _, chunk := range splitMessage(message, maxLength) {
		if err := t.sendChunk(chunk); err != nil {
			return err
		}
	}
	return nil
}

func (t *Telegram) sendChunk(text string) error {
	url := fmt.Sprintf(telegramAPI, t.token)
	body, _ := json.Marshal(map[string]string{
		"chat_id":    t.chatID,
		"text":       text,
		"parse_mode": "Markdown",
	})

	var lastErr error
	for attempt := 1; attempt <= maxRetries; attempt++ {
		resp, err := t.client.Post(url, "application/json", bytes.NewReader(body))
		if err == nil {
			defer resp.Body.Close()
			if resp.StatusCode < 200 || resp.StatusCode >= 300 {
				lastErr = fmt.Errorf("telegram API returned %d", resp.StatusCode)
			} else {
				return nil
			}
		} else {
			lastErr = err
		}
		if attempt < maxRetries {
			time.Sleep(retryDelay)
		}
	}
	return fmt.Errorf("after %d attempts: %w", maxRetries, lastErr)
}

// splitMessage breaks msg at newline boundaries so each chunk fits within max bytes.
func splitMessage(msg string, max int) []string {
	if len(msg) <= max {
		return []string{msg}
	}
	var chunks []string
	for len(msg) > 0 {
		end := max
		if end > len(msg) {
			end = len(msg)
		}
		if end < len(msg) {
			if i := strings.LastIndex(msg[:end], "\n"); i > 0 {
				end = i + 1
			}
		}
		chunks = append(chunks, msg[:end])
		msg = msg[end:]
	}
	return chunks
}
