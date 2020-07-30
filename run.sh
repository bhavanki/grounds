#!/usr/bin/env bash

# A convenient script for running Grounds with (or without) a security manager.
# With a security manager in place, and using the provided policy, softcode is
# restricted from most sensitive JVM operations while the core game remains
# almost completely unrestricted. (See GroundsSecurityManager for details.)
#
# With a security manager:
# ./run.sh --secure --properties etc/server.properties ...
#
# Without a security manager:
# ./run.sh --properties etc/server.properties ...

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
CMD=("java")

JAR=$(find "${DIR}" -name "grounds-*.jar" | head -n 1)
if [[ -z $JAR ]]; then
  echo "JAR is missing, build grounds first"
  exit 1
fi

if [[ $1 == --secure ]]; then

  POLICY=$(find "${DIR}" -name "java.policy" | head -n 1)
  if [[ -z $POLICY ]]; then
    echo "Policy java.policy is missing"
    exit 1
  fi

  CMD+=("-Djar.dir=$(dirname "${JAR}")")
  CMD+=("-Djava.security.manager=xyz.deszaras.grounds.server.GroundsSecurityManager")
  CMD+=("-Djava.security.policy=${POLICY}")
  shift
fi

CMD+=("-jar" "${JAR}")

"${CMD[@]}" "$@"
