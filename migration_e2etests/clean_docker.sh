#!/bin/bash -x

# usage: clean-docker.sh [--all]

DOCKER_COMPOSE_PREFIX=migratione2etests

if [[ $1 == "--all" ]]; then
    echo "Removing all processess.."
    docker ps -a -q --filter name="$DOCKER_COMPOSE_PREFIX" | xargs -r docker rm -f -v
else
    echo "Removing exited processes..."
    docker ps -q --filter status=exited,name="$DOCKER_COMPOSE_PREFIX" | xargs -r docker rm -v
fi

echo "Removing unused networks..."
docker network ls -q --filter type=custom,name="$DOCKER_COMPOSE_PREFIX" | xargs -r docker network rm 2> /dev/null
