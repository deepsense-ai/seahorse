#!/usr/bin/env bash

# The aim of this script is to clean all components
# It means the script will remove `build`, `bower_components` and `node_modules` directories
# from each respective component

YELLOW='\033[1;33m'
NC='\033[0m' # No Color

find . -maxdepth 1 -type d \( ! -name . \) -exec bash -c \
    "cd '{}' \
    && echo -e \"\n\n${YELLOW}cleaning component {}${NC}\" \
    && rm -rf build node_modules bower_components" \;

echo -e "\n\n${YELLOW}Clean finished${NC}\n\n"
