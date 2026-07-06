#!/usr/bin/env bash
# Convenience script: build everything, then run the API server.
# Open dashboard/index.html in a browser separately once this is running.
#
# TODO(person-B): once this is stable, consider turning it into the
# "documented local run" deliverable mentioned in the plan, and/or record
# the demo video off the back of this script.
set -euo pipefail

cd "$(dirname "$0")/.."

echo "Building all modules..."
mvn -q -pl engine-core,api-server -am install -DskipTests

echo "Starting api-server on http://localhost:8080 ..."
cd api-server
mvn spring-boot:run
