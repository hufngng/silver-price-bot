package fetcher

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

const goldpriceURL = "https://data-asg.goldprice.org/dbXRates/USD"

type goldpriceFetcher struct {
	client *http.Client
	loc    *time.Location
}

func NewGoldprice(loc *time.Location) Fetcher {
	return &goldpriceFetcher{
		client: &http.Client{Timeout: 10 * time.Second},
		loc:    loc,
	}
}

func (f *goldpriceFetcher) Name() string { return "goldprice" }

func (f *goldpriceFetcher) Fetch(ctx context.Context) (string, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, goldpriceURL, nil)
	if err != nil {
		return "", err
	}
	req.Header.Set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
	req.Header.Set("Referer", "https://goldprice.org/")

	resp, err := f.client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	var result struct {
		Items []struct {
			XagPrice float64 `json:"xagPrice"`
			PcXag    float64 `json:"pcXag"`
		} `json:"items"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return "", err
	}
	if len(result.Items) == 0 {
		return "", fmt.Errorf("empty items in response")
	}

	item := result.Items[0]
	sign := "+"
	if item.PcXag < 0 {
		sign = ""
	}
	now := time.Now().In(f.loc).Format("02/01/2006 15:04:05")

	return fmt.Sprintf(
		"🥈 *Silver Price - USD/oz*\n"+
			"💰 Price: `$%.2f`\n"+
			"📈 Change: `%s%.2f%%`\n"+
			"🕐 Updated: `%s`\n"+
			"🔗 Source: goldprice.org",
		item.XagPrice, sign, item.PcXag, now,
	), nil
}
