#!/usr/bin/env bash

# The aim of this script is to build (or rebuild) all components located in this directory
# Build process is controlled by respective component's gulpfile and is not matter of this script

YELLOW='\033[1;33m'
NC='\033[0m' # No Color

find . -maxdepth 1 -type d \( ! -name . \) -exec bash -c \
    "cd '{}' \
    && echo -e \"\n\n${YELLOW}Building component {}${NC}\" \
    && gulp clean \
    && gulp" \;

echo -e "\n\n${YELLOW}Build finished${NC}\n\n"
