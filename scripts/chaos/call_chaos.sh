#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function usage {
    echo "usage: $0 <port> <customers|visits|vets> <attacks_enable_exception|attacks_enable_killapplication|attacks_enable_latency|attacks_enable_memory|watcher_enable_component|watcher_enable_controller|watcher_enable_repository|watcher_enable_restcontroller|watcher_enable_service|watcher_disable>"
    echo "First specify the port, then pick either customers, visits, or vets"
    echo "Then pick what to enable. Order matters!"
    echo "Example"
    echo "$0 8082 visits attacks_enable_exception watcher_enable_restcontroller"
    exit 1
}

if [[ $# -lt 3 ]]; then
    usage
fi

PORT="$1"
shift

while [[ $# > 0 ]]
do
key="$1"
case $1 in
    customers)
        ;;
    visits)
        ;;
    vets)
        ;;
    attacks*)
        ( cd "${ROOT_DIR}" && curl "http://localhost:${PORT}/actuator/chaosmonkey/assaults" -H "Content-Type: application/json" --data @"${1}".json --fail )
        ;;
    watcher*)
        ( cd "${ROOT_DIR}" && curl "http://localhost:${PORT}/actuator/chaosmonkey/watchers" -H "Content-Type: application/json" --data @"${1}".json --fail )
        ;;
    *)
        usage
        ;;
esac
shift
done

