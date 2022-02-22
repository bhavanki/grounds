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

    local param_value="$2"
    # shellcheck disable=SC2076
    if [[ $param_value =~ "|" ]]; then
      param_block="${param_block}["
      IFS='|' read -r -a elements <<< "$param_value"
      for (( i = 0; i < ${#elements[@]}; i++ )); do
        param_block="${param_block}\"${elements[$i]}\""
        if (( i < ${#elements[@]} - 1 )); then
          param_block="${param_block},"
        fi
      done
      param_block="${param_block}]"
    else
      param_block="${param_block}\"${param_value}\""
    fi

    param_block="${param_block},"

    shift 2
  done

  local request_id
  request_id=$(uuidgen)

  nc -U "$GROUNDS_API_SOCKET_PATH" <<EOF
{
  "jsonrpc" : "2.0",
  "method" : "${method}",
  "params" : {
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

BALL_RESPONSES=(
  "It is certain"
  "Without a doubt"
  "You may rely on it"
  "Yes definitely"
  "It is decidedly so"
  "As I see it, yes"
  "Most likely"
  "Yes"
  "Outlook good"
  "Signs point to yes"
  "Reply hazy try again"
  "Better not tell you now"
  "Ask again later"
  "Cannot predict now"
  "Concentrate and ask again"
  "Don't count on it"
  "Outlook not so good"
  "My sources say no"
  "Very doubtful"
  "My reply is no"
)

read -r -d '' REQUEST

METHOD=$(echo "$REQUEST" | jq -r '.method' -)
PLUGIN_CALL_ID=$(echo "$REQUEST" | jq -r '.params._plugin_call_id' -)
REQUEST_ID=$(echo "$REQUEST" | jq -r '.id' -)

CALLER_NAME_RESPONSE=$(jsonrpc "${PLUGIN_CALL_ID}" getCallerName)
CALLER_NAME=$(echo "$CALLER_NAME_RESPONSE" | jq -r '.result')

POSE_CONTENT=

case $METHOD in
  coinflip)
    NUM=$(( RANDOM % 2 ))
    if (( NUM == 0 )); then
      SIDE=heads
    else
      SIDE=tails
    fi
    POSE_CONTENT="${CALLER_NAME} flips a coin: ${SIDE}"
    ;;
  8ball)
    INDEX=$(( RANDOM % ${#BALL_RESPONSES[@]} ))
    POSE_CONTENT="${CALLER_NAME} shakes the magic eight ball: ${BALL_RESPONSES[$INDEX]}"
    ;;
  roll)
    readarray -t PLUGIN_CALL_ARGUMENTS < <(echo "$REQUEST" | jq -r '.params._plugin_call_arguments[]')
    if (( ${#PLUGIN_CALL_ARGUMENTS[@]} != 1 )); then
      respond_error "${INVALID_PARAMETERS}" "Missing roll-type argument" "$REQUEST_ID"
    fi
    ROLL_TYPE=${PLUGIN_CALL_ARGUMENTS[0]}
    if [[ ! $ROLL_TYPE =~ [1-9][0-9]*d[1-9][0-9]* ]]; then
      respond_error "${INVALID_PARAMETERS}" "Invalid argument: $ROLL_TYPE" "$REQUEST_ID"
    fi
    NUM_SIDES=${ROLL_TYPE##*d}
    NUM_THROWS=${ROLL_TYPE%%d*}
    ROLL_RESULTS=()
    ROLL_TOTAL=0
    for (( i = 0; i < NUM_THROWS; i++ )); do
      ROLL=$(( (RANDOM % NUM_SIDES) + 1 ))
      ROLL_RESULTS+=( "$ROLL" )
      ROLL_TOTAL=$(( ROLL_TOTAL + ROLL ))
    done
    POSE_CONTENT="${CALLER_NAME} rolls: [ ${ROLL_RESULTS[*]} ] total: ${ROLL_TOTAL}"
    ;;
  *)
    respond_error "$METHOD_NOT_FOUND" "Unrecognized method ${METHOD}" "$REQUEST_ID"
    ;;
esac

POSE_RESPONSE=$(jsonrpc "${PLUGIN_CALL_ID}" exec commandLine "pose|${POSE_CONTENT}")
PARSED_RESPONSE=$(parse_response "$POSE_RESPONSE")
if [[ $PARSED_RESPONSE =~ ^r: ]]; then
  respond_result "" "$REQUEST_ID"
else
  PARSED_ERROR=${PARSED_RESPONSE:2}
  respond_error "${INTERNAL_ERROR}" "POSE error: [${PARSED_ERROR%%:*}] ${PARSED_ERROR#*:}" "$REQUEST_ID"
fi
