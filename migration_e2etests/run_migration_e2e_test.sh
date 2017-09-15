#!/bin/bash -x

SCRIPT_DIR=$(dirname $0)

. $SCRIPT_DIR/common.sh > /dev/null

export PROJECT_ROOT

function cleanup() {
    for dir in "${WORKDIR_NO_TIMESTAMP}"*; do
        if [[ -d $dir ]]; then
            pushd $dir > /dev/null
            docker-compose kill > /dev/null
            if [[ ! -f docker-compose.log ]]; then
                docker-compose logs > docker-compose.log
            fi
            docker-compose down -v > /dev/null
            popd > /dev/null
        fi
    done
    $SCRIPT_DIR/clean_docker.sh --all > /dev/null
}

cleanup

if [[ $CLEANUP_ONLY  ]]; then
    exit 0;
fi;

trap cleanup EXIT

WORKDIR=$($SCRIPT_DIR/prepare_env.sh -b $BACKEND_TAG -f $FRONTEND_TAG) || exit 1

pushd "$WORKDIR" > /dev/null

docker-compose up -d > /dev/null

pushd "$PROJECT_ROOT" > /dev/null

sbt "e2etests/clean" \
    "e2etests/test:testOnly io.deepsense.e2etests.session.AllExampleWorkflowsWorkOnLocalClusterSessionTest" \
    "e2etests/test:testOnly io.deepsense.e2etests.batch.AllExampleWorkflowsWorkOnLocalClusterBatchTest"

popd > /dev/null

popd > /dev/null
