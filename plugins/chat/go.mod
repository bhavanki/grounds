module github.com/bhavanki/grounds-chat

go 1.17

require (
	github.com/bhavanki/groundsapi v0.0.0
	github.com/urfave/cli/v2 v2.3.0
	golang.org/x/exp/jsonrpc2 v0.0.0-20220328175248-053ad81199eb
)

require (
	github.com/cpuguy83/go-md2man/v2 v2.0.0-20190314233015-f79a8a8ca69d // indirect
	github.com/russross/blackfriday/v2 v2.0.1 // indirect
	github.com/shurcooL/sanitized_anchor_name v1.0.0 // indirect
	golang.org/x/exp/event v0.0.0-20220217172124-1812c5b45e43 // indirect
	golang.org/x/xerrors v0.0.0-20200804184101-5ec99f83aff1 // indirect
)

replace github.com/bhavanki/groundsapi => ../grounds-api-go
