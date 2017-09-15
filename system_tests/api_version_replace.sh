#!/bin/bash

usage() {
    echo "Replaces API version in workflow files"
    echo "Usage: $0 VERSION"
    exit 1
}

if [ -z "$1" ]; then
    usage
fi

find workflow_executor/resources -type f -name *.json -exec\
    perl -p -i -e "s/\"apiVersion\": \"(\d+.\d+.\d+)?\"/\"apiVersion\": \"$1\"/g" {} \;
