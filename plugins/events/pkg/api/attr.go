package api

import (
	"encoding/json"
	"fmt"
	"strconv"
	"time"
)

const (
	AttrTypeString    = "STRING"
	AttrTypeInteger   = "INTEGER"
	AttrTypeBoolean   = "BOOLEAN"
	AttrTypeTimestamp = "TIMESTAMP"
	AttrTypeThing     = "THING"
	AttrTypeAttr      = "ATTR"
	AttrTypeAttrList  = "ATTRLIST"
)

type Attr struct {
	Name  string `json:"name"`
	Value string `json:"value"`
	Type  string `json:"type"`
}

func NewAttrListAttr(name string, value []Attr) (Attr, error) {
	listJson, err := json.Marshal(value)
	if err != nil {
		return Attr{}, err
	}
	return Attr{
		Name:  name,
		Value: string(listJson),
		Type:  AttrTypeAttrList,
	}, nil
}

func NewAttrListAttrFromMap(name string, value map[string]Attr) (Attr, error) {
	attrs := make([]Attr, 0, len(value))
	for _, a := range value {
		attrs = append(attrs, a)
	}
	return NewAttrListAttr(name, attrs)
}

func NewStringAttr(name string, value string) Attr {
	return Attr{
		Name:  name,
		Value: value,
		Type:  AttrTypeString,
	}
}

func NewTimestampAttr(name string, value time.Time) Attr {
	return Attr{
		Name:  name,
		Value: strconv.FormatInt(value.Unix(), 10),
		Type:  AttrTypeTimestamp,
	}
}

func (a Attr) GetAttrListValue() (map[string]Attr, error) {
	if a.Type != AttrTypeAttrList {
		return nil, fmt.Errorf("Attr is of type %s", a.Type)
	}
	var as []Attr
	err := json.Unmarshal([]byte(a.Value), &as)
	if err != nil {
		return nil, err
	}
	m := make(map[string]Attr, len(as))
	for _, asa := range as {
		m[asa.Name] = asa
	}
	return m, nil
}

func (a Attr) GetAttrInAttrListValue(name string) (Attr, error) {
	if a.Type != AttrTypeAttrList {
		return Attr{}, fmt.Errorf("Attr is of type %s", a.Type)
	}
	var as []Attr
	err := json.Unmarshal([]byte(a.Value), &as)
	if err != nil {
		return Attr{}, err
	}
	for _, asa := range as {
		if asa.Name == name {
			return asa, nil
		}
	}
	return Attr{}, fmt.Errorf("Attribute %s not found", name)
}
