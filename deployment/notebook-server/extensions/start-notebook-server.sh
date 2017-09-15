#!/bin/bash
# Copyright (c) 2015, CodiLime Inc.

/opt/conda/envs/python2/bin/python /usr/local/share/jupyter/kernels/pyspark/session_executor_mock.py &
SESSION_EXECUTOR_MOCK_PID=$!

. start-notebook.sh

kill $SESSION_EXECUTOR_MOCK_PID