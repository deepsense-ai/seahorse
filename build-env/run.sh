#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.

set -ex

docker build -t seahorse/build-env .
docker run -v "$(pwd)/data:/data" -v "$(pwd)/cache:/cache" -it seahorse/build-env

cd data

# WorkflowManager
cd deepsense-backend-tap
docker build -t deepsense-workflowmanager workflowmanager/target/docker/stage
cd ..

# SessionManager
cd deepsense-backend-tap
docker build -t deepsense-sessionmanager sessionmanager/target/docker/stage
cd ..

# Frontend
cd deepsense-frontend-tap
docker build -t deepsense-frontend deepsense-frontend/docker
cd ..

# Proxy
cd deepsense-frontend-tap
docker build -t deepsense-proxy proxy
cd ..

# RabbitMQ
cd deepsense-backend-tap
docker build -t deepsense-rabbitmq deployment/ansible/roles/rabbitmq_dockerized/files
cd ..

# Notebooks
cd deepsense-backend-tap
docker build -t deepsense-notebook remote_notebook
cd ..
