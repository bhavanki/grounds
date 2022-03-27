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
	// err should be of type *jsonrpc2.wireError
	errType := reflect.TypeOf(err)
	if errType.Kind() != reflect.Ptr {
		return -2
	}
	derefType := errType.Elem()
	if derefType.Kind() != reflect.Struct {
		return -1
	}
	codeField, ok := derefType.FieldByName("Code")
	if !ok || codeField.Type.Kind() != reflect.Int64 {
		return 0
	}
	return reflect.ValueOf(err).Elem().FieldByName("Code").Int()
}
