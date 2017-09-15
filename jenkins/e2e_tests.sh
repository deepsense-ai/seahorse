#!/usr/bin/env bash

(cd deployment/docker-compose ; ./docker-compose pull)
(cd deployment/docker-compose ; ./docker-compose up -d)

sbt e2etests/clean e2etests/test
SBT_EXIT_CODE=$?

(cd deployment/docker-compose ; ./docker-compose down)
exit $SBT_EXIT_CODE
