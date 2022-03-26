package api

import (
	"context"
	"net"
	"reflect"

	"golang.org/x/exp/jsonrpc2"
)

const (
	groundsApiSocketFile = "/tmp/groundsapi.sock"

	ErrCodeNotFound = -32004
)

func newApiClient(ctx context.Context) (*jsonrpc2.Connection, error) {
	return jsonrpc2.Dial(ctx,
		jsonrpc2.NetDialer("unix", groundsApiSocketFile, net.Dialer{}),
		jsonrpc2.ConnectionOptions{
			Framer: jsonrpc2.RawFramer(),
		},
	)
}

func Call(ctx context.Context, method string, params map[string]interface{},
	result interface{}) error {
	client, err := newApiClient(ctx)
	if err != nil {
		return err
	}
	if params == nil {
		params = make(map[string]interface{})
	}
	params["_plugin_call_id"] = ctx.Value(PluginCallIdKey).(string)
	asyncCall := client.Call(ctx, method, params)
	if result != nil {
		return asyncCall.Await(ctx, result)
	} else {
		var ignored string
		return asyncCall.Await(ctx, &ignored)
	}
}

func ErrorCode(err error) int64 {
	errType := reflect.TypeOf(err)
	codeField, ok := errType.FieldByName("Code")
	if !ok || codeField.Type.Kind() != reflect.Int64 {
		return 0
	}
	return reflect.ValueOf(err).FieldByName("Code").Int()
}
