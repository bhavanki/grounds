#!/usr/bin/env bash
#
# Grounds plugin for commands involving randomness.

GROUNDS_API_SOCKET_PATH=/tmp/groundsapi.sock
METHOD_NOT_FOUND=-32601
INVALID_PARAMETERS=-32602
INTERNAL_ERROR=-32603

jsonrpc() {
  local plugin_call_id=$1
  local method=$2
  shift 2

  local param_block=""
  while (( $# > 0 )); do
    local param_name=$1
    param_block="${param_block}\"${param_name}\":"

    local param_type=$2
    local param_value=$3
    case "$param_type" in
      string)
        param_block="${param_block}\"${param_value}\""
        ;;
      boolean)
        param_block="${param_block}${param_value}"
        ;;
      stringlist)
        param_block="${param_block}["
        IFS='|' read -r -a elements <<< "$param_value"
        for (( i = 0; i < ${#elements[@]}; i++ )); do
          param_block="${param_block}\"${elements[$i]}\""
          if (( i < ${#elements[@]} - 1 )); then
            param_block="${param_block},"
          fi
        done
        param_block="${param_block}]"
        ;;
    esac

    param_block="${param_block},"

    shift 3
  done

  local request_id
  request_id=$(uuidgen)

  nc -U "$GROUNDS_API_SOCKET_PATH" <<EOF
{
  "jsonrpc" : "2.0",
  "method" : "${method}",
  "parameters" : {
${param_block}
    "_plugin_call_id" : "${plugin_call_id}"
  },
  "id" : "${request_id}"
}
EOF
}

parse_response() {
  local response=$1
  local result
  result=$(echo "$response" | jq -r '.result' --exit-status -)
  # shellcheck disable=SC2181
  if (( $? == 0 )); then
    echo "r:${result}"
  else
    local error_code
    error_code=$(echo "$response" | jq -r '.error.code' -)
    local error_message
    error_message=$(echo "$response" | jq -r '.error.message' -)
    echo "e:${error_code}:${error_message}"
  fi
}

respond_result() {
  local result=$1
  local request_id=$2
  cat <<EOF
{
  "jsonrpc": "2.0",
  "result": "${result}",
  "id": "${request_id}"
}
EOF
  exit 0
}

respond_error() {
  local code=$1
  local message=$2
  local request_id=$3
  cat <<EOF
{
  "jsonrpc": "2.0",
  "error": {
    "code": ${code},
    "message": "${message}"
  },
  "id": "${request_id}"
}
EOF
  exit 1
}

read -r -d '' REQUEST

METHOD=$(echo "$REQUEST" | jq -r '.method' -)
PLUGIN_CALL_ID=$(echo "$REQUEST" | jq -r '.parameters._plugin_call_id' -)
REQUEST_ID=$(echo "$REQUEST" | jq -r '.id' -)

if [[ $METHOD != "_listen" ]]; then
  respond_error "$METHOD_NOT_FOUND" "Unrecognized method ${METHOD}" "$REQUEST_ID"
fi

readarray -t PLUGIN_CALL_ARGUMENTS < <(echo "$REQUEST" | jq -r '.parameters._plugin_call_arguments[]')
if (( ${#PLUGIN_CALL_ARGUMENTS[@]} != 1 )); then
  respond_error "${INVALID_PARAMETERS}" "Missing payload argument" "$REQUEST_ID"
fi
PAYLOAD=${PLUGIN_CALL_ARGUMENTS[0]}

PLAYER=$(echo "$PAYLOAD" | jq -r '.player' -)
POSE_CONTENT="The magic fiddle plays a ditty as ${PLAYER} arrives."
POSE_RESPONSE=$(jsonrpc "${PLUGIN_CALL_ID}" exec commandLine stringlist "pose|${POSE_CONTENT}" asExtension boolean "true")
PARSED_RESPONSE=$(parse_response "$POSE_RESPONSE")
if [[ $PARSED_RESPONSE =~ ^r: ]]; then
  respond_result "" "$REQUEST_ID"
else
  PARSED_ERROR=${PARSED_RESPONSE:2}
  respond_error "${INTERNAL_ERROR}" "POSE error: [${PARSED_ERROR%%:*}] ${PARSED_ERROR#*:}" "$REQUEST_ID"
fi
