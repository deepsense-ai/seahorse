#!/bin/bash

# Copyright (c) 2016, CodiLime, Inc.
#
# Installs Notebooks dependencies.
# Takes parameters (all parameters are required):
# --working-dir
# --mq-host
# --mq-port
# --workflow-id
# --session-id

while [[ $# > 1 ]]
do
key="$1"

case $key in
  -d|--working-dir)
  WORKING_DIR="$2"
  shift # past argument
  ;;
  --python-binary)
  PYTHON_BINARY="$2"
  shift # past argument
  ;;
  -a|--additional-python-path)
  ADDITIONAL_PYTHON_PATH="$2"
  shift # past argument
  ;;
  --gateway-host)
  GATEWAY_HOST="$2"
  shift # past argument
  ;;
  --gateway-port)
  GATEWAY_PORT="$2"
  shift # past argument
  ;;
  -h|--mq-host)
  MQ_HOST="$2"
  shift # past argument
  ;;
  -p|--mq-port)
  MQ_PORT="$2"
  shift # past argument
  ;;
  --mq-user)
  MQ_USER="$2"
  shift # past argument
  ;;
  --mq-pass)
  MQ_PASS="$2"
  shift # past argument
  ;;
  -w|--workflow-id)
  WORKFLOW_ID="$2"
  shift # past argument
  ;;
  -s|--session-id)
  SESSION_ID="$2"
  shift # past argument
  ;;
  *)
  echo "Unknown option: $key"
  exit -1
  ;;
esac
shift # past argument or value
done

# Verifying if all required parameters are set
if [ -z "$WORKING_DIR" ];  then echo "Parameter --working-dir is required"; exit -1; fi
if [ -z "$PYTHON_BINARY" ]; then echo "Parameter --python-binary is required"; exit -1; fi
if [ -z "$ADDITIONAL_PYTHON_PATH" ]; then echo "Parameter --additional-python-path is required"; exit -1; fi
if [ -z "$GATEWAY_HOST" ]; then echo "Parameter --gateway-host is required"; exit -1; fi
if [ -z "$GATEWAY_PORT" ]; then echo "Parameter --gateway-port is required"; exit -1; fi
if [ -z "$MQ_HOST" ];      then echo "Parameter --mq-host is required"; exit -1; fi
if [ -z "$MQ_PORT" ];      then echo "Parameter --mq-port is required"; exit -1; fi
if [ -z "$MQ_USER" ];      then echo "Parameter --mq-user is required"; exit -1; fi
if [ -z "$MQ_PASS" ];      then echo "Parameter --mq-pass is required"; exit -1; fi
if [ -z "$WORKFLOW_ID" ];  then echo "Parameter --workflow-id is required"; exit -1; fi
if [ -z "$SESSION_ID" ];   then echo "Parameter --session-id is required"; exit -1; fi

# Exit script after first erroneous instruction
set -ex

cd $WORKING_DIR

echo "INSTALLING DEPENDENCIES"

PYTHON_DIRECTORY=$(dirname $(dirname $PYTHON_BINARY))

export PATH=$PYTHON_DIRECTORY/bin:$PATH
export LOCAL_PATH=$(pwd)/local-packages
pip install --root $LOCAL_PATH pika-0.10.0.tar.gz

export PYTHONPATH="$LOCAL_PATH/$PYTHON_DIRECTORY/lib/python2.7/site-packages/:$ADDITIONAL_PYTHON_PATH"
echo "PYTHONPATH=$PYTHONPATH"

sed -i s#PYTHON_BINARY#"$PYTHON_BINARY"# executing_kernel/kernel.json

echo "start executing_kernel_manager"
exec $PYTHON_BINARY executing_kernel/executing_kernel_manager.py \
  --gateway-host "$GATEWAY_HOST" \
  --gateway-port "$GATEWAY_PORT" \
  --mq-host "$MQ_HOST" \
  --mq-port "$MQ_PORT" \
  --mq-user "$MQ_USER" \
  --mq-pass "$MQ_PASS" \
  --workflow-id "$WORKFLOW_ID" \
  --session-id "$SESSION_ID"
