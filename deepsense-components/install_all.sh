#!/usr/bin/env bash

# The goal of this script is to install dependencies for all components in this directory
# The list if dependencies is located in respective component's package.json file and is not matter of this script

YELLOW='\033[1;33m'
NC='\033[0m' # No Color
start=`date +%s`
NPM_REGISTRY_URL="https://registry.npmjs.org"

npm set registry $NPM_REGISTRY_URL

find . -maxdepth 1 -type d \( ! -name . \) -exec bash -c \
    "cd '{}' \
    && echo -e \"\n\n${YELLOW}Installing dependencies for {}${NC}\" \
    && npm install" \;

end=`date +%s`
runtime=$((end-start))

echo -e "\n\n${YELLOW}Install finished in ${runtime} seconds${NC}"
trap times EXIT
