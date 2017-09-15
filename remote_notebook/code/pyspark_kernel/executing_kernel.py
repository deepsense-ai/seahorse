# Copyright 2016 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



import sys
import os

import zmq
from ipykernel.ipkernel import IPythonKernel
from ipykernel.kernelapp import IPKernelApp
from traitlets import Type


class ExecutingKernel(IPythonKernel):
    """
    This is where the actual code execution happens.

    This kernel listens on messages sent via RabbitMQ and forwards them
    to appropriate ZMQ sockets of the actual IPython kernel. Similarly,
    all traffic sent via ZMQ sockets ends up in RabbitMQ.
    """

    def __init__(self, **kwargs):
        super(ExecutingKernel, self).__init__(**kwargs)

class ExecutingKernelApp(IPKernelApp):
    kernel_class = Type(ExecutingKernel, config=True)
    log_level = 'DEBUG'


if __name__ == '__main__':
    app = ExecutingKernelApp.instance(context=zmq.Context.instance())
    app.initialize()
    app.start()
