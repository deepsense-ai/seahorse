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
  -a|--additional-python-path)
  ADDITIONAL_PYTHON_PATH="$2"
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
if [ -z "$WORKING_DIR" ]; then echo "Parameter --working-dir is required"; exit -1; fi
if [ -z "$ADDITIONAL_PYTHON_PATH" ]; then echo "Parameter --additional-python-path is required"; exit -1; fi
if [ -z "$MQ_HOST" ];     then echo "Parameter --mq-host is required"; exit -1; fi
if [ -z "$MQ_PORT" ];     then echo "Parameter --mq-port is required"; exit -1; fi
if [ -z "$WORKFLOW_ID" ]; then echo "Parameter --workflow-id is required"; exit -1; fi
if [ -z "$SESSION_ID" ];  then echo "Parameter --session-id is required"; exit -1; fi


echo "DIAGNOSTIC CHECKS"
echo "python processes"
ps uax

echo "executables"
ls -l /usr/bin/ /usr/sbin/

echo "whoami"
whoami

echo "pip"
pip -V

echo "pip2"
pip2 -V

echo "pip3"
pip3 -V

echo "python"
which python

echo "pyspark"
pyspark --version

echo "python3"
python3 --version

echo "system issue"
cat /etc/issue

echo "uname"
uname -a

echo "listing pip packages"
pip list



# Exit script after first erroneous instruction
set -ex
echo "INSTALLING DEPENDENCIES"

PWD=`pwd`
echo "PWD=$PWD"

echo "download get_pip"
wget https://bootstrap.pypa.io/get-pip.py

echo "install pip"
python2.7 get-pip.py -I --root $PWD

export PIP_PATH="$PWD/usr/bin/pip"
echo "PIP_PATH=$PIP_PATH"

PREFIX_PATH="usr"

export PYTHONPATH="$PWD/usr/lib/python2.7/dist-packages:$PWD/usr/lib/python2.7:$PWD/usr/lib/python2.7/site-packages:$ADDITIONAL_PYTHON_PATH"
echo "PYTHONPATH=$PYTHONPATH"

$PIP_PATH install --install-option="--prefix=$PREFIX_PATH" -I --root $PWD ipykernel
$PIP_PATH install --install-option="--prefix=$PREFIX_PATH" -I --root $PWD jupyter_client
$PIP_PATH install --install-option="--prefix=$PREFIX_PATH" -I --root $PWD pika

# STARTING EXECUTING KERNEL MANAGER
# echo "listing the CWD"

cd $WORKING_DIR

echo "start executing_kernel_manager"
python2.7 executing_kernel_manager.py \
  --mq-host "$MQ_HOST" \
  --mq-port "$MQ_PORT" \
  --workflow-id "$WORKFLOW_ID" \
  --session-id "$SESSION_ID"
