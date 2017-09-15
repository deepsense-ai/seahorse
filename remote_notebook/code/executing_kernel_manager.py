# Copyright (c) 2016, CodiLime Inc.

import os
from threading import Event

from jupyter_client import MultiKernelManager
from jupyter_client.kernelspec import KernelSpecManager

from rabbit_mq_client import RabbitMQClient, RabbitMQJsonReceiver, RabbitMQJsonSender
from utils import debug
import argparse
import signal


class ExecutingKernelManager(object):
    """
    This is the class implementing the main process on the remote host.

    It's role is to manage the lifecycle of all ExecutingKernels, in particular
    start them and shut them down.
    """

    EXCHANGE = 'remote_notebook_kernel'
    ALL_MANAGEMENT_SUBSCRIPTION_TOPIC = 'management.{session_id}.*.to_manager'

    SX_EXCHANGE = 'seahorse'
    SX_PUBLISHING_TOPIC = 'kernelmanager.{session_id}.{workflow_id}.from'

    EXECUTING_KERNEL_NAME = 'ExecutingKernel'

    def __init__(self, gateway_address, rabbit_mq_address, rabbit_mq_credentials,
                 session_id, workflow_id, executing_kernel_source_dir):
        super(ExecutingKernelManager, self).__init__()

        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

        self._executing_kernel_source_dir = executing_kernel_source_dir
        self._gateway_address = gateway_address
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
        debug('ExecutingKernelManager::start: Starting.')
        self._rabbit_listener.subscribe(
            topic=self.ALL_MANAGEMENT_SUBSCRIPTION_TOPIC.format(session_id=self._session_id),
            handler=self._handle_management_message)

        # Send ready nofitication
        self._sx_sender.send({
            "messageType": "kernelManagerReady",
            "messageBody": {}
        })

        # Without the timeout, this is un-interruptable.
        while not self._shutdown_event.is_set():
            self._shutdown_event.wait(1)

        debug('ExecutingKernelManager::start: Shutting down kernels.')
        self._multi_kernel_manager.shutdown_all(now=True)
        debug('ExecutingKernelManager::start: Bye.')

    def stop(self):
        self._shutdown_event.set()

    def exit_gracefully(self, signum, frame):
        self.stop()

    def _handle_management_message(self, message):
        known_message_types = ['start_kernel', 'shutdown_kernel']
        if not isinstance(message, dict) or 'type' not in message or message['type'] not in known_message_types:
            debug('ExecutingKernelManager::_handle_management_message: Unknown message: {}'.format(message))
            self._shutdown_event.set()
            return

        if message['type'] == 'start_kernel':
            node_id = message['node_id'] if 'node_id' in message else None
            port_number = message['port_number'] if 'port_number' in message else None
            self.start_kernel(kernel_id=message['kernel_id'],
                              signature_key=message['signature_key'],
                              node_id=node_id,
                              port_number=port_number)

        elif message['type'] == 'shutdown_kernel':
            self.shutdown_kernel(kernel_id=message['kernel_id'])

    def start_kernel(self, kernel_id, signature_key, node_id, port_number):
        debug('ExecutingKernelManager::start_kernel: kernel_id {}, signature key {}'.format(kernel_id, signature_key))

        if kernel_id in self._multi_kernel_manager.list_kernel_ids():
            debug('ExecutingKernelManager::start_kernel: kernel_id {}. Shutting down before restart.'.format(kernel_id))
            self._multi_kernel_manager.shutdown_kernel(kernel_id, now=True)

        extra_arguments = ['--',
                           '--signature-key', signature_key,
                           '--kernel-id', kernel_id,
                           '--gateway-host', self._gateway_address[0],
                           '--gateway-port', str(self._gateway_address[1]),
                           '--mq-host', self._rabbit_mq_address[0],
                           '--mq-port', str(self._rabbit_mq_address[1]),
                           '--mq-user', self._rabbit_mq_credentials[0],
                           '--mq-pass', str(self._rabbit_mq_credentials[1]),
                           '--workflow-id', self._workflow_id
                           ]

        if node_id is not None:
            extra_arguments.extend(['--node-id', node_id])

        if port_number is not None:
            extra_arguments.extend(['--port-number', str(port_number)])

        debug('ExecutingKernelManager::start_kernel: extra_arguments = {}'.format(extra_arguments))

        self._multi_kernel_manager.start_kernel(kernel_name=self.EXECUTING_KERNEL_NAME,
                                                kernel_id=kernel_id,
                                                extra_arguments=extra_arguments)

    def shutdown_kernel(self, kernel_id):
        debug('ExecutingKernelManager::shutdown_kernel: kernel_id {}'.format(kernel_id))
        self._multi_kernel_manager.shutdown_kernel(kernel_id=kernel_id)

    def _init_rabbit_client(self):
        rabbit_client = RabbitMQClient(address=self._rabbit_mq_address,
                                       credentials=self._rabbit_mq_credentials,
                                       exchange=self.EXCHANGE)
        return RabbitMQJsonReceiver(rabbit_client)

    def _init_kernel_manager(self):
        mkm = MultiKernelManager()
        mkm.log_level = 'DEBUG'
        mkm.kernel_spec_manager = KernelSpecManager()
        mkm.kernel_spec_manager.kernel_dirs.append(self._executing_kernel_source_dir + '/share/jupyter/kernels')
        mkm.kernel_spec_manager.install_kernel_spec(source_dir=self._executing_kernel_source_dir,
                                                    kernel_name=self.EXECUTING_KERNEL_NAME,
                                                    prefix=self._executing_kernel_source_dir)
        return mkm


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--gateway-host', action='store', dest='gateway_host')
    parser.add_argument('--gateway-port', action='store', dest='gateway_port')
    parser.add_argument('--mq-host', action='store', dest='mq_host')
    parser.add_argument('--mq-port', action='store', dest='mq_port')
    parser.add_argument('--mq-user', action='store', dest='mq_user')
    parser.add_argument('--mq-pass', action='store', dest='mq_pass')
    parser.add_argument('--session-id',  action='store', dest='session_id')
    parser.add_argument('--workflow-id', action='store', dest='workflow_id')
    args = parser.parse_args()

    gateway_address = (args.gateway_host, int(args.gateway_port))
    mq_address = (args.mq_host, int(args.mq_port))
    mq_credentials = (args.mq_user, args.mq_pass)

    kernel_source_dir = os.path.join(os.getcwd(), 'executing_kernel')
    ekm = ExecutingKernelManager(gateway_address, mq_address, mq_credentials, args.session_id,
                                 args.workflow_id, executing_kernel_source_dir=kernel_source_dir)
    ekm.run()
