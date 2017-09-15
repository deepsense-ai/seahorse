# Copyright (c) 2016, CodiLime Inc.


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

    def _exit(self, msg):
        self.logger.debug(msg)
        sys.exit(1)


class ExecutingKernelApp(IPKernelApp):
    kernel_class = Type(ExecutingKernel, config=True)
    log_level = 'DEBUG'


if __name__ == '__main__':
    app = ExecutingKernelApp.instance(context=zmq.Context.instance())
    app.initialize()
    app.start()
