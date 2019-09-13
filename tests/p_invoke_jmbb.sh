#!/bin/sh -e
# auxiliary procedure to invoke JMBB
root="$(cd "$(dirname "$0")/.." && pwd)"
exec java -jar "$root/jmbb.jar" "$@"
