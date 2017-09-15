#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

cd `dirname $0`"/../../"

./jenkins/manage-docker.py --sync --publish --all
