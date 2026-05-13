package fetcher

import "context"

type Fetcher interface {
	Fetch(ctx context.Context) (string, error)
	Name() string
}
