package groundsapi

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"

	"golang.org/x/exp/jsonrpc2"
)

const (
	ErrCodeParseError     = -32700
	ErrCodeInvalidRequest = -32600
	ErrCodeMethodNotFound = -32601
	ErrCodeInvalidParams  = -32602
	ErrCodeInternalError  = -32603
)

type SubcommandHandler func(context.Context, *PluginCall) (interface{}, error)

type SubcommandDispatcher func(context.Context, PluginCall) SubcommandHandler

type PluginHandler struct {
	Method     string
	Dispatcher SubcommandDispatcher
}

func (h PluginHandler) Handle(ctx context.Context, req *jsonrpc2.Request) *jsonrpc2.Response {
	if req.Method != h.Method {
		return newJsonRpcErrorResponse(
			ErrCodeMethodNotFound,
			fmt.Sprintf("Unrecognized method %s", req.Method),
			req.ID,
		)
	}

	ctx, call, err := newPluginCall(ctx, req)
	if err != nil {
		return newJsonRpcErrorResponse(
			ErrCodeInvalidParams,
			fmt.Sprintf("Invalid plugin call: %s", err.Error()),
			req.ID,
		)
	}
	if len(call.Arguments) < 1 {
		return newJsonRpcErrorResponse(
			ErrCodeInvalidParams,
			"At least one plugin call argument is required",
			req.ID,
		)
	}

	subcommandHandler := h.Dispatcher(ctx, *call)
	if subcommandHandler == nil {
		return newJsonRpcErrorResponse(
			ErrCodeInvalidParams,
			fmt.Sprintf("Unrecognized subcommand %s", call.Arguments[0]),
			req.ID,
		)
	}
	result, err := subcommandHandler(ctx, call)

	if result != nil {
		res, err := jsonrpc2.NewResponse(req.ID, result, nil)
		if err != nil {
			return newJsonRpcErrorResponse(
				ErrCodeInternalError,
				"Failed to marshal result",
				req.ID,
			)
		}
		return res
	} else {
		return newJsonRpcErrorResponse(
			ErrCodeInternalError,
			err.Error(),
			req.ID,
		)
	}
}

func ParseJsonRpcRequest(r io.Reader) (*jsonrpc2.Request, error) {
	b, err := io.ReadAll(r)
	if err != nil {
		return nil, err
	}
	msg, err := jsonrpc2.DecodeMessage(b)
	if err != nil {
		return nil, err
	}
	req := msg.(*jsonrpc2.Request)
	return req, nil
}

type PluginCall struct {
	id          string
	method      string
	ExtensionId string
	Arguments   []string
}

type ContextKey string

const (
	PluginCallIdKey = ContextKey("plugin_call_id")
)

func newPluginCall(ctx context.Context, req *jsonrpc2.Request) (context.Context, *PluginCall, error) {
	var params map[string]interface{}
	if err := json.Unmarshal(req.Params, &params); err != nil {
		return ctx, nil, err
	}

	call := PluginCall{
		method: req.Method,
	}

	switch pci := params["_plugin_call_id"].(type) {
	case string:
		call.id = pci
	default:
		return ctx, nil, errors.New("Plugin call ID not a string")
	}
	ctx = context.WithValue(ctx, PluginCallIdKey, call.id)

	switch ei := params["_extension_id"].(type) {
	case string:
		call.ExtensionId = ei
	default:
		return ctx, nil, errors.New("Extension ID not a string")
	}

	switch pca := params["_plugin_call_arguments"].(type) {
	case []interface{}:
		{
			args := make([]string, 0, len(pca))
			for i, element := range pca {
				switch arg := element.(type) {
				case string:
					args = append(args, arg)
				default:
					return ctx, nil, fmt.Errorf("Element %d in plugin call arguments is not a string: %T",
						i, arg)
				}
			}
			call.Arguments = args
		}
	default:
		return ctx, nil, fmt.Errorf("Plugin call arguments not a string list: %T", pca)
	}

	return ctx, &call, nil
}

func CheckArgumentCount(call *PluginCall, expected int) error {
	if len(call.Arguments) != expected {
		return fmt.Errorf("Expected %d plugin call arguments, got %d",
			expected, len(call.Arguments))
	}
	return nil
}

func CheckArgumentCountAtLeast(call *PluginCall, expected int) error {
	if len(call.Arguments) < expected {
		return fmt.Errorf("Expected at least %d plugin call arguments, got %d",
			expected, len(call.Arguments))
	}
	return nil
}

func newJsonRpcErrorResponse(code int64, message string, id jsonrpc2.ID) *jsonrpc2.Response {
	res, _ := jsonrpc2.NewResponse(id, nil, jsonrpc2.NewError(code, message))
	return res
}
