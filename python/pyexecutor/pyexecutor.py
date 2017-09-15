# Copyright (c) 2015, CodiLime Inc.

import time

from py4j.java_gateway import JavaGateway, GatewayClient, java_import
from py4j.protocol import Py4JError

from code_executor import CodeExecutor
from gateway_resolver import GatewayResolver
from pyspark import SparkContext, SQLContext, SparkConf

RABBIT_MQ_ADDRESS = ("localhost", 5672)


class PyExecutor(object):
    def __init__(self, mq_address):
        self.gateway_resolver = GatewayResolver(mq_address)

    def run(self):
        gateway = self._initialize_gateway(
            gateway_address=self.gateway_resolver.get_gateway_address())
        if not gateway:
            print 'Failed to initialize java gateway'
            return

        # noinspection PyProtectedMember
        callback_server_port = gateway._callback_server.server_socket.getsockname()[1]
        spark_context, sql_context = self._initialize_spark_contexts(gateway)
        code_executor = CodeExecutor(spark_context, sql_context, gateway.entry_point)

        try:
            gateway.entry_point.reportCallbackServerPort(callback_server_port)
            gateway.entry_point.registerCodeExecutor(code_executor)
        except Py4JError:
            print 'Exception while registering codeExecutor, or callback server port'
            gateway.close()
            return

        # Wait for the end of the world
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            print 'Exiting on user\'s request'
            gateway.close()

    @staticmethod
    def _initialize_spark_contexts(gateway):
        java_spark_context = gateway.entry_point.getSparkContext()
        java_spark_conf = java_spark_context.getConf()

        spark_context = SparkContext(
            conf=SparkConf(_jvm=gateway.jvm, _jconf=java_spark_conf),
            gateway=gateway,
            jsc=java_spark_context)

        sql_context = SQLContext(spark_context)

        return spark_context, sql_context

    @staticmethod
    def _initialize_gateway(gateway_address):
        (host, port) = gateway_address
        gateway = JavaGateway(GatewayClient(address=host, port=port),
                              python_proxy_port=0,
                              start_callback_server=True,
                              auto_convert=True)
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
        except Py4JError:
            print 'Error while initializing java gateway'
            gateway.close()
            return None

        return gateway


def main():
    py_executor = PyExecutor(mq_address=RABBIT_MQ_ADDRESS)
    py_executor.run()


if __name__ == '__main__':
    main()
