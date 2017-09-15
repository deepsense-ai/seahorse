#!/usr/bin/env bash

# The aim of this script is to build (or rebuild) all components located in this directory
# Build process is controlled by respective component's gulpfile and is not matter of this script

YELLOW='\033[1;33m'
NC='\033[0m' # No Color
start=`date +%s`

find . -maxdepth 1 -type d \( ! -name . \) -exec bash -c \
    "cd '{}' \
    && echo -e \"\n\n${YELLOW}Building component {}${NC}\" \
    && gulp clean \
    && gulp" \;

end=`date +%s`
runtime=$((end-start))

echo -e "\n\n${YELLOW}Build finished in ${runtime} seconds${NC}\n\n"
trap times EXIT
