#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.

set -e

[ -d /data ] || (echo "Mount volume /data and re-run the script." ; exit 1)

mkdir /cache/.sbt  || true
mkdir /cache/.ivy2 || true
mkdir /cache/.m2   || true

cd /data

# Clone repos
[ -d seahorse-workflow-executor ] || git clone https://github.com/deepsense-io/seahorse-workflow-executor.git
[ -d deepsense-backend-tap      ] || git clone https://github.com/codilime/deepsense-backend-tap.git
[ -d deepsense-frontend-tap     ] || git clone https://github.com/codilime/deepsense-frontend-tap.git

# SessionExecutor
cd seahorse-workflow-executor
git checkout seahorse_on_tap
sbt sPublishLocal workflowexecutor/assembly
cd ..

# WorkflowManager
cd deepsense-backend-tap
sbt -Dlocal.resolver workflowmanager/docker:stage
cd ..

# SessionManager
cd deepsense-backend-tap
sbt -Dlocal.resolver -Dworkflowexecutor.jar=file:///data/seahorse-workflow-executor/workflowexecutor/target/scala-2.10/workflowexecutor.jar sessionmanager/docker:stage
cd ..

# Frontend
cd deepsense-frontend-tap
cd deepsense-frontend
./build.sh
cp -r build docker/
cd ..
