# Copyright (c) 2016, CodiLime Inc.

import os
from threading import Event
import json

from jupyter_client import MultiKernelManager
from jupyter_client.kernelspec import KernelSpecManager

from rabbit_mq_client import RabbitMQClient, RabbitMQJsonReceiver, RabbitMQJsonSender
from utils import setup_logging, Logging
import argparse
import signal

from executing_kernel_client import ExecutingKernelClient, ExecutingKernelClientSettings


class ExecutingKernelManager(Logging):
    """
    This is the class implementing the main process on the remote host.

    It's role is to manage the lifecycle of all ExecutingKernels, in particular
    start them and shut them down.
    """

    EXCHANGE = 'remote_notebook_kernel'
    ALL_MANAGEMENT_SUBSCRIPTION_TOPIC = 'management.{session_id}.*.to_manager'

    SX_EXCHANGE = 'seahorse'
    SX_PUBLISHING_TOPIC = 'kernelmanager.{session_id}.{workflow_id}.from'

    PYTHON_EXECUTING_KERNEL_NAME = 'PythonExecutingKernel'
    R_EXECUTING_KERNEL_NAME = 'RExecutingKernel'

    def __init__(self, gateway_address, r_backend_address,
                 rabbit_mq_address, rabbit_mq_credentials,
                 session_id, workflow_id, kernels_source_dir,
                 py_executing_kernel_source_dir,
                 r_executing_kernel_source_dir):
        super(ExecutingKernelManager, self).__init__()

        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

        self.executing_kernel_clients = {}

        self._kernels_source_dir = kernels_source_dir
        self._py_executing_kernel_source_dir = py_executing_kernel_source_dir
        self._r_executing_kernel_source_dir = r_executing_kernel_source_dir
        self._gateway_address = gateway_address
        self._r_backend_address = r_backend_address
        self._rabbit_mq_address = rabbit_mq_address
        self._rabbit_mq_credentials = rabbit_mq_credentials
        self._session_id = session_id
        self._workflow_id = workflow_id

        self._shutdown_event = Event()
        self._multi_kernel_manager = self._init_kernel_manager()
        self._rabbit_listener = self._init_rabbit_client()
        self._sx_sender = RabbitMQJsonSender(
            rabbit_mq_client=RabbitMQClient(address=self._rabbit_mq_address,
                                            credentials=self._rabbit_mq_credentials,
                                            exchange=self.SX_EXCHANGE),
            topic=self.SX_PUBLISHING_TOPIC.format(session_id=self._session_id,
                                                  workflow_id=self._workflow_id))

    def run(self):
        self.logger.debug('Starting.')
        self._rabbit_listener.subscribe(
            topic=self.ALL_MANAGEMENT_SUBSCRIPTION_TOPIC.format(session_id=self._session_id),
            handler=self._handle_management_message)

        # Send ready notification
        self._sx_sender.send({
            'messageType': 'kernelManagerReady',
            'messageBody': {}
        })

        # Without the timeout, this is un-interruptable.
        while not self._shutdown_event.is_set():
            if os.getppid() == 1:
                self.logger.debug("I'm an orphan - stopping")
                self.stop()
            self._shutdown_event.wait(1)

        self.logger.debug('Shutting down kernels.')
        self._multi_kernel_manager.shutdown_all(now=True)
        self.logger.debug('Bye.')

    def stop(self):
        self._shutdown_event.set()

    def exit_gracefully(self, signum, frame):
        self.stop()

    def _handle_management_message(self, message):
        known_message_types = ['start_kernel', 'shutdown_kernel']
        if not isinstance(message, dict) or 'type' not in message or message['type'] not in known_message_types:
            self.logger.debug('Unknown message: {}'.format(message))
            self._shutdown_event.set()
            return

        if message['type'] == 'start_kernel':
            dataframe_storage_type = message['dataframe_source']['dataframe_storage_type']
            node_id = message['dataframe_source']['node_id'] if 'node_id' in message['dataframe_source'] else None
            port_number = message['dataframe_source']['port_number'] if 'port_number' in message['dataframe_source'] else None
            kernel_name = message['kernel_name']
            self.start_kernel(kernel_id=message['kernel_id'],
                              signature_key=message['signature_key'],
                              node_id=node_id,
                              port_number=port_number,
                              kernel_name=kernel_name,
                              dataframe_storage_type=dataframe_storage_type)

        elif message['type'] == 'shutdown_kernel':
            self.shutdown_kernel(kernel_id=message['kernel_id'])

    def start_kernel(self, kernel_id, signature_key, node_id, port_number, kernel_name, dataframe_storage_type):
        self.logger.debug('kernel_id {}, signature key {}'.format(kernel_id, signature_key))

        if kernel_id in self._multi_kernel_manager.list_kernel_ids():
            self.logger.debug('kernel_id {}. Shutting down before restart.'.format(kernel_id))
            self._multi_kernel_manager.shutdown_kernel(kernel_id, now=True)

        def with_replaced_key(real_factory):
            def hacked_factory(**kwargs):
                km = real_factory(**kwargs)
                km.session.key = signature_key
                return km

            return hacked_factory

        self._multi_kernel_manager.kernel_manager_factory = with_replaced_key(
            self._multi_kernel_manager.kernel_manager_factory)

        self._multi_kernel_manager.start_kernel(kernel_name=kernel_name,
                                                kernel_id=kernel_id)

        settings = ExecutingKernelClientSettings(self._gateway_address, self._r_backend_address,
                                                 self._rabbit_mq_address,
                                                 self._rabbit_mq_credentials, self._session_id,
                                                 self._workflow_id, node_id, port_number, dataframe_storage_type)
        executing_kernel_client = ExecutingKernelClient(kernel_id, signature_key, settings)
        executing_kernel_client.start()
        self.executing_kernel_clients[kernel_id] = executing_kernel_client

    def shutdown_kernel(self, kernel_id):
        self.logger.debug('kernel_id {}'.format(kernel_id))
        self._multi_kernel_manager.shutdown_kernel(kernel_id=kernel_id)
        self.executing_kernel_clients.pop(kernel_id)

    def _init_rabbit_client(self):
        rabbit_client = RabbitMQClient(address=self._rabbit_mq_address,
                                       credentials=self._rabbit_mq_credentials,
                                       exchange=self.EXCHANGE)
        return RabbitMQJsonReceiver(rabbit_client)

    def _init_kernel_manager(self):
        mkm = MultiKernelManager()
        mkm.log_level = 'DEBUG'
        mkm.kernel_spec_manager = KernelSpecManager()
        mkm.kernel_spec_manager.kernel_dirs.append(
          self._kernels_source_dir + '/share/jupyter/kernels')
        mkm.kernel_spec_manager.install_kernel_spec(source_dir=self._py_executing_kernel_source_dir,
                                                    kernel_name=self.PYTHON_EXECUTING_KERNEL_NAME,
                                                    prefix=self._kernels_source_dir)

        mkm.kernel_spec_manager.install_kernel_spec(source_dir=self._r_executing_kernel_source_dir,
                                                    kernel_name=self.R_EXECUTING_KERNEL_NAME,
                                                    prefix=self._kernels_source_dir)
        return mkm


if __name__ == '__main__':
    setup_logging(os.path.join(os.getcwd(), 'executing_kernel_manager.log'))

    parser = argparse.ArgumentParser()
    parser.add_argument('--gateway-host', action='store', dest='gateway_host')
    parser.add_argument('--gateway-port', action='store', dest='gateway_port')
    parser.add_argument('--r-backend-host', action='store', dest='r_backend_host')
    parser.add_argument('--r-backend-port', action='store', dest='r_backend_port')
    parser.add_argument('--mq-host', action='store', dest='mq_host')
    parser.add_argument('--mq-port', action='store', dest='mq_port')
    parser.add_argument('--mq-user', action='store', dest='mq_user')
    parser.add_argument('--mq-pass', action='store', dest='mq_pass')
    parser.add_argument('--session-id',  action='store', dest='session_id')
    parser.add_argument('--workflow-id', action='store', dest='workflow_id')
    args = parser.parse_args()

    gateway_address = (args.gateway_host, int(args.gateway_port))
    r_backend_address = (args.r_backend_host, int(args.r_backend_port))
    mq_address = (args.mq_host, int(args.mq_port))
    mq_credentials = (args.mq_user, args.mq_pass)

    kernels_source_dir = os.path.join(os.getcwd(), 'executing_kernels')
    py_kernel_source_dir = os.path.join(kernels_source_dir, 'python')
    r_kernel_source_dir = os.path.join(kernels_source_dir, 'r')
    ekm = ExecutingKernelManager(gateway_address, r_backend_address,
                                 mq_address, mq_credentials,
                                 args.session_id, args.workflow_id,
                                 kernels_source_dir=kernels_source_dir,
                                 py_executing_kernel_source_dir=py_kernel_source_dir,
                                 r_executing_kernel_source_dir=r_kernel_source_dir)
    ekm.run()
