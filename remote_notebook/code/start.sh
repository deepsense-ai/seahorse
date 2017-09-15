#!/bin/bash

# Copyright (c) 2015, CodiLime, Inc.
#
# Installs Notebooks dependencies.
# Takes parameters (all parameters are required):
# --mq-host
# --mq-port
# --workflow-id
# --session-id

while [[ $# > 1 ]]
do
key="$1"

case $key in
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
set -e
echo "INSTALLING DEPENDENCIES"

PWD=`pwd`
echo "PWD=$PWD"

echo "download get_pip"
wget https://bootstrap.pypa.io/get-pip.py

echo "install pip"
python2.7 get-pip.py --root $PWD

PIP_PATH="$PWD/usr/bin/pip"
echo "PIP_PATH=$PIP_PATH"

PYTHONPATH="$PWD/usr/lib/python2.7/site-packages"
echo "PYTHONPATH=$PYTHONPATH"

$PWD/usr/bin/pip install --root $PWD ipykernel
$PWD/usr/bin/pip install --root $PWD jupyter_client
$PWD/usr/bin/pip install --root $PWD pika

echo "unzip executing_kernel_manager"
unzip -d executing_kernel notebook_executing_kernel.zip



# STARTING EXECUTING KERNEL MANAGER
echo "listing the CWD"
ls -laR

echo "start executing_kernel_manager"
/usr/bin/python2.7 executing_kernel/executing_kernel_manager.py \
  --mq-host "$MQ_HOST" \
  --mq-port "$MQ_PORT" \
  --workflow-id "$WORKFLOW_ID" \
  --session-id "$SESSION_ID"

