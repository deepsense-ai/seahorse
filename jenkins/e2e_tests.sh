#!/usr/bin/env bash




(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG pull)
# destroy dockercompose_default, so we can recreate it with proper id
(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG down)
(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG up -d)


sbt e2etests/clean e2etests/test
SBT_EXIT_CODE=$?

(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG down)
exit $SBT_EXIT_CODE
