#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

TASKS="clean scalastyle test:scalastyle test it:compile ds-it"
#sbt -DSPARK_VERSION=2.0.0 $TASKS
#sbt -DSPARK_VERSION=2.0.1 $TASKS
#sbt -DSPARK_VERSION=2.1.0 $TASKS
sbt -DSPARK_VERSION=2.1.1 $TASKS
sbt -DSPARK_VERSION=2.0.2 $TASKS
sbt -DSPARK_VERSION=1.6.1 $TASKS
