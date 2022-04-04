package groundsapi

import (
	"context"
)

func HasAttr(ctx context.Context, thingId string, name string, asExtension bool) (bool, error) {
	params := map[string]interface{}{
		"thingId":     thingId,
		"name":        name,
		"_as_extension": asExtension,
	}
	var a Attr
	err := Call(ctx, "getAttr", params, &a)
	if err != nil {
		if ErrorCode(err) == ErrCodeNotFound {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

func GetAttr(ctx context.Context, thingId string, name string, asExtension bool) (Attr, error) {
	params := map[string]interface{}{
		"thingId":     thingId,
		"name":        name,
		"_as_extension": asExtension,
	}
	var a Attr
	err := Call(ctx, "getAttr", params, &a)
	if err != nil {
		return Attr{}, err
	}
	return a, nil
}

func GetAttrNames(ctx context.Context, thingId string, asExtension bool) ([]string, error) {
	params := map[string]interface{}{
		"thingId":     thingId,
		"_as_extension": asExtension,
	}
	var names []string
	err := Call(ctx, "getAttrNames", params, &names)
	if err != nil {
		return nil, err
	}
	return names, nil
}

func GetCallerName(ctx context.Context) (string, error) {
	var n string
	err := Call(ctx, "getCallerName", nil, &n)
	if err != nil {
		return "", err
	}
	return n, nil
}

func GetCallerTZ(ctx context.Context) (string, error) {
	var tz string
	err := Call(ctx, "getCallerTimezone", nil, &tz)
	if err != nil {
		return "", err
	}
	return tz, nil
}

func SendMessage(ctx context.Context, playerName string, message string) error {
	params := map[string]interface{}{
		"playerName": playerName,
		"message":    message,
	}
	return Call(ctx, "sendMessage", params, nil)
}

func SendMessageToCaller(ctx context.Context, message string) error {
	params := map[string]interface{}{
		"message": message,
	}
	return Call(ctx, "sendMessageToCaller", params, nil)
}

func SendMessageWithHeaderToCaller(ctx context.Context, message string, header string) error {
	params := map[string]interface{}{
		"message": message,
		"header":  header,
	}
	return Call(ctx, "sendMessageToCaller", params, nil)
}

func SendRecordToCaller(ctx context.Context, record map[string][]string, header string) error {
	params := map[string]interface{}{
		"record": record,
		"header": header,
	}
	return Call(ctx, "sendMessageToCaller", params, nil)
}

func SendTableToCaller(ctx context.Context, table map[string][][]string, header string) error {
	params := map[string]interface{}{
		"table":  table,
		"header": header,
	}
	return Call(ctx, "sendMessageToCaller", params, nil)
}

func RemoveAttr(ctx context.Context, thingId string, name string, asExtension bool) error {
	params := map[string]interface{}{
		"thingId":     thingId,
		"name":        name,
		"_as_extension": asExtension,
	}
	return Call(ctx, "removeAttr", params, nil)
}

func SetAttr(ctx context.Context, thingId string, a Attr, asExtension bool) error {
	params := map[string]interface{}{
		"thingId":     thingId,
		"name":        a.Name,
		"value":       a.Value,
		"type":        a.Type,
		"_as_extension": asExtension,
	}
	return Call(ctx, "setAttr", params, nil)
}

func SetAttrInAttrListValue(ctx context.Context, thingId string, name string, subAttr Attr, asExtension bool) error {
	a, err := GetAttr(ctx, thingId, name, asExtension)
	if err != nil {
		return err
	}
	attrList, err := a.GetAttrListValue()
	if err != nil {
		return err
	}

	attrList[subAttr.Name] = subAttr

	newa, err := NewAttrListAttrFromMap(name, attrList)
	if err != nil {
		return err
	}
	return SetAttr(ctx, thingId, newa, asExtension)
}
