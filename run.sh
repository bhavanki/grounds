#!/usr/bin/env bash

# A convenient script for running Grounds.
#
# ./run.sh --properties etc/server.properties ...

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
CMD=("java")

JAR=$(find "${DIR}" -name "grounds-*.jar" | head -n 1)
if [[ -z $JAR ]]; then
  echo "JAR is missing, build grounds first"
  exit 1
fi

CMD+=("-jar" "${JAR}")

"${CMD[@]}" "$@"
