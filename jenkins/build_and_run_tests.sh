#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

TASKS="clean scalastyle test:scalastyle test it:compile ds-it"
sbt -DsparkVersion=2.0.0 $TASKS
sbt -DsparkVersion=1.6.1 $TASKS
