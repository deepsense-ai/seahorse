# Copyright (c) 2015, CodiLime Inc.
import argparse
import time

from py4j.java_gateway import JavaGateway, GatewayClient, CallbackServerParameters, java_import
from py4j.protocol import Py4JError

from code_executor import CodeExecutor
from simple_logging import log_debug, log_error
from pyspark.sql import SparkSession
from pyspark import SparkContext, SparkConf


class PyExecutor(object):
    def __init__(self, gateway_address):
        self.gateway_address = gateway_address

    def run(self):
        gateway = self._initialize_gateway(self.gateway_address)
        if not gateway:
            log_error('Failed to initialize java gateway')
            return

        # noinspection PyProtectedMember
        callback_server_port = gateway._callback_server.server_socket.getsockname()[1]
        spark_context, sql_context = self._initialize_spark_contexts(gateway)
        code_executor = CodeExecutor(spark_context, sql_context, gateway.entry_point)

        try:
            gateway.entry_point.registerCallbackServerPort(callback_server_port)
            gateway.entry_point.registerCodeExecutor(code_executor)
        except Py4JError as e:
            log_error('Exception while registering codeExecutor, or callback server port: {}'.format(e))
            gateway.close()
            return

        # Wait for the end of the world
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            log_debug('Exiting on user\'s request')
            gateway.close()

    @staticmethod
    def _initialize_spark_contexts(gateway):
        java_spark_context = gateway.entry_point.getSparkContext()
        java_spark_conf = java_spark_context.getConf()

        spark_context = SparkContext(
            conf=SparkConf(_jvm=gateway.jvm, _jconf=java_spark_conf),
            gateway=gateway,
            jsc=java_spark_context)

        sql_context = SparkSession(spark_context, gateway.entry_point.getSparkSession())

        return spark_context, sql_context

    @staticmethod
    def _initialize_gateway(gateway_address):
        (host, port) = gateway_address

        callback_params = CallbackServerParameters(address=host, port=0)

        gateway = JavaGateway(GatewayClient(address=host, port=port),
                              start_callback_server=True,
                              auto_convert=True,
                              callback_server_parameters = callback_params)
        try:
            java_import(gateway.jvm, "org.apache.spark.SparkEnv")
            java_import(gateway.jvm, "org.apache.spark.SparkConf")
            java_import(gateway.jvm, "org.apache.spark.api.java.*")
            java_import(gateway.jvm, "org.apache.spark.api.python.*")
            java_import(gateway.jvm, "org.apache.spark.mllib.api.python.*")
            java_import(gateway.jvm, "org.apache.spark.sql.*")
            java_import(gateway.jvm, "org.apache.spark.sql.hive.*")
            java_import(gateway.jvm, "scala.Tuple2")
            java_import(gateway.jvm, "scala.collection.immutable.List")
        except Py4JError as e:
            log_error('Error while initializing java gateway: {}'.format(e))
            gateway.close()
            return None

        return gateway


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--gateway-address', action='store')
    args = parser.parse_args()

    gateway_address = args.gateway_address.split(':')
    gateway_address = (gateway_address[0], int(gateway_address[1]))

    py_executor = PyExecutor(gateway_address=gateway_address)
    py_executor.run()


if __name__ == '__main__':
    main()
