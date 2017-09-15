# usage: [-b BACKEND_TAG] [-f FRONTEND_TAG] [--cleanup-only]
CMDLINE_PARAMS=$(getopt -l cleanup-only -- b:f: $@) || exit 1

set -- $CMDLINE_PARAMS

while [[ $@ ]]; do
    case "$1" in
        -b)
            BACKEND_TAG=$(eval echo "$2")
            shift; shift
            ;;
        -f)
            FRONTEND_TAG=$(eval echo "$2")
            shift; shift
            ;;
        --cleanup-only)
            CLEANUP_ONLY=true
            shift
            ;;
        *)
            shift
            ;;
    esac
done

SCRIPT_DIR=$(pushd $(dirname $0) > /dev/null; pwd; popd > /dev/null)

if [[ -z $PROJECT_ROOT ]]; then
    pushd $SCRIPT_DIR
    PROJECT_ROOT=$(git worktree list --porcelain | grep worktree | head -1 | awk '{print $2}')
    popd
fi

pushd $PROJECT_ROOT
GIT_SHA=$(git rev-parse HEAD)
popd

BACKEND_TAG=${BACKEND_TAG:-$GIT_SHA}

FRONTEND_TAG=${FRONTEND_TAG:-$SEAHORSE_BUILD_TAG}
FRONTEND_TAG=${FRONTEND_TAG:-master-latest}

WORKDIR_NO_TIMESTAMP=$(pwd)/migration_e2etests.${BACKEND_TAG}.${FRONTEND_TAG}
