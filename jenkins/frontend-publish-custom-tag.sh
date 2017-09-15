#!/bin/bash -ex

# Copyright (c) 2016, CodiLime Inc.
#
# Publish frontend docker to private docker-repo using custom tag 
# $CUSTOM_TAG required for setting to what tag the image will be pushed

CUSTOM_TAG="${CUSTOM_TAG?Need to set CUSTOM_TAG}"

# Enter main directory
cd `dirname $0`"/../"

./publish.sh ${CUSTOM_TAG}

