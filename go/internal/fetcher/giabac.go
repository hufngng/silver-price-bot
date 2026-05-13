package fetcher

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/PuerkitoBio/goquery"
)

const giabacURL = "https://giabac.vn/"
const giabacFilterURL = "https://giabac.vn/SilverInfo/FilterData"

type giabacFetcher struct {
	client *http.Client
	loc    *time.Location
}

func NewGiaBac(loc *time.Location) Fetcher {
	return &giabacFetcher{
		client: &http.Client{Timeout: 10 * time.Second},
		loc:    loc,
	}
}

func (f *giabacFetcher) Name() string { return "giabac" }

type priceEntry struct {
	buy  string
	sell string
}

func (f *giabacFetcher) fetchUnit(ctx context.Context, filterType string) (priceEntry, error) {
	body := url.Values{"filterType": {filterType}}.Encode()
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, giabacFilterURL, strings.NewReader(body))
	if err != nil {
		return priceEntry{}, err
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
	req.Header.Set("Referer", giabacURL)
	req.Header.Set("X-Requested-With", "XMLHttpRequest")

	resp, err := f.client.Do(req)
	if err != nil {
		return priceEntry{}, err
	}
	defer resp.Body.Close()

	doc, err := goquery.NewDocumentFromReader(resp.Body)
	if err != nil {
		return priceEntry{}, err
	}

	prices := doc.Find("p.text-24px")
	if prices.Length() < 2 {
		return priceEntry{}, fmt.Errorf("price elements not found for filterType=%s", filterType)
	}
	return priceEntry{
		buy:  strings.TrimSpace(prices.Eq(0).Text()),
		sell: strings.TrimSpace(prices.Eq(1).Text()),
	}, nil
}

func (f *giabacFetcher) fetchUpdatedAt(ctx context.Context) string {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, giabacURL, nil)
	if err != nil {
		return ""
	}
	req.Header.Set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
	req.Header.Set("Referer", giabacURL)

	resp, err := f.client.Do(req)
	if err != nil {
		return ""
	}
	defer resp.Body.Close()

	doc, err := goquery.NewDocumentFromReader(resp.Body)
	if err != nil {
		return ""
	}

	// e.g. "Bảng giá bạc Phú Quý  cập nhật 17:57 13/05/2026"
	text := strings.TrimSpace(doc.Find("#priceTable p.text-12px.fw-bold").Text())
	if i := strings.Index(text, "cập nhật "); i >= 0 {
		return strings.TrimSpace(text[i+len("cập nhật "):])
	}
	return ""
}

func (f *giabacFetcher) Fetch(ctx context.Context) (string, error) {
	chi, err := f.fetchUnit(ctx, "#pills-home")
	if err != nil {
		return "", fmt.Errorf("chi: %w", err)
	}
	luong, err := f.fetchUnit(ctx, "#pills-profile")
	if err != nil {
		return "", fmt.Errorf("luong: %w", err)
	}
	kg, err := f.fetchUnit(ctx, "#pills-contact")
	if err != nil {
		return "", fmt.Errorf("kg: %w", err)
	}

	updatedAt := f.fetchUpdatedAt(ctx)
	if updatedAt == "" {
		updatedAt = time.Now().In(f.loc).Format("15:04 02/01/2006")
	}

	return fmt.Sprintf(
		"🥈 *Silver 999 - giabac.vn*\n\n"+
			"🏷 *Per Chi* _(VND/chi)_\n"+
			"  Buy:  `%s`\n"+
			"  Sell: `%s`\n\n"+
			"🏷 *Per Luong* _(VND/luong)_\n"+
			"  Buy:  `%s`\n"+
			"  Sell: `%s`\n\n"+
			"🏷 *Per Kilogram* _(VND/kg)_\n"+
			"  Buy:  `%s`\n"+
			"  Sell: `%s`\n\n"+
			"🕐 Updated: `%s`\n"+
			"🔗 giabac.vn",
		chi.buy, chi.sell,
		luong.buy, luong.sell,
		kg.buy, kg.sell,
		updatedAt,
	), nil
}
