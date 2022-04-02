package groundsapi

import (
	"encoding/json"
)

func UnmarshalEventPayload(payload string, m *map[string]interface{}) error {
	return json.Unmarshal([]byte(payload), m)
}
