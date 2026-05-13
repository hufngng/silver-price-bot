package fetcher

import (
	"context"
	"fmt"
	"net/http"
	"strings"
	"time"

	"github.com/PuerkitoBio/goquery"
)

const muavangbacURL = "https://muavangbac.vn/"

type muavangbacFetcher struct {
	client *http.Client
	loc    *time.Location
}

func NewMuaVangBac(loc *time.Location) Fetcher {
	return &muavangbacFetcher{
		client: &http.Client{Timeout: 10 * time.Second},
		loc:    loc,
	}
}

func (f *muavangbacFetcher) Name() string { return "muavangbac" }

func (f *muavangbacFetcher) Fetch(ctx context.Context) (string, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, muavangbacURL, nil)
	if err != nil {
		return "", err
	}
	req.Header.Set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
	req.Header.Set("Referer", "https://muavangbac.vn/")

	resp, err := f.client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	doc, err := goquery.NewDocumentFromReader(resp.Body)
	if err != nil {
		return "", err
	}

	// XAG/USD: second .crypto-price span (first is gold)
	cryptoPrices := doc.Find("span.crypto-price")
	if cryptoPrices.Length() < 2 {
		return "", fmt.Errorf("XAG price element not found")
	}
	xagText := strings.TrimSpace(cryptoPrices.Eq(1).Text())

	// Phu Quy sections: each span.silver-icon-pq is inside an h2,
	// the sibling div contains buy/sell prices
	pqSections := doc.Find("span.silver-icon-pq")
	if pqSections.Length() < 2 {
		return "", fmt.Errorf("Phu Quy price elements not found")
	}

	extractPrices := func(idx int) (buy, sell string) {
		priceDiv := pqSections.Eq(idx).Closest("h2").Next()
		prices := priceDiv.Find("span.gold-price.d-none.d-lg-block")
		clean := func(s string) string {
			s = strings.TrimSpace(s)
			// strip everything from "đ" onward
			if i := strings.Index(s, "đ"); i > 0 {
				s = strings.TrimSpace(s[:i])
			}
			return s
		}
		if prices.Length() >= 2 {
			buy = clean(prices.Eq(0).Text())
			sell = clean(prices.Eq(1).Text())
		}
		return
	}

	bmBuy, bmSell := extractPrices(0)
	btBuy, btSell := extractPrices(1)
	now := time.Now().In(f.loc).Format("02/01/2006 15:04:05")

	return fmt.Sprintf(
		"🥈 *Silver Price - muavangbac.vn*\n\n"+
			"🌐 *XAG/USD:* `$%s`\n\n"+
			"🏷 *Phu Quy Silver Piece* _(VND/tael)_\n"+
			"  Buy:  `%s`\n"+
			"  Sell: `%s`\n\n"+
			"🏷 *Phu Quy Silver Bar 1kg* _(VND/kg)_\n"+
			"  Buy:  `%s`\n"+
			"  Sell: `%s`\n\n"+
			"🕐 `%s`\n"+
			"🔗 muavangbac.vn",
		xagText, bmBuy, bmSell, btBuy, btSell, now,
	), nil
}
