#!/usr/bin/env bash

./jenkins/scripts/checkout-submodules.sh

(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG pull)
# destroy dockercompose_default, so we can recreate it with proper id
(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG down)
(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG up -d)

sbt e2etests/clean publishWeClasses e2etests/test
SBT_EXIT_CODE=$?

(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG down)
exit $SBT_EXIT_CODE
