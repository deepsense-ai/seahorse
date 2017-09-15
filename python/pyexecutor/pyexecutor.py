# Copyright (c) 2015, CodiLime Inc.
import argparse
import os
import time
from py4j.java_gateway import JavaGateway, GatewayClient, CallbackServerParameters, java_import
from py4j.protocol import Py4JError
from pyspark import SparkContext, SparkConf

from code_executor import CodeExecutor
from simple_logging import log_debug, log_error


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
        spark_context, spark_sql_session = self._initialize_spark_contexts(gateway)
        code_executor = CodeExecutor(spark_context, spark_sql_session, gateway.entry_point)

        try:
            gateway.entry_point.registerCallbackServerPort(callback_server_port)
            gateway.entry_point.registerCodeExecutor(code_executor)
        except Py4JError as e:
            log_error('Exception while registering codeExecutor, or callback server port: {}'.format(e))
            gateway.close()
            return

        # Wait for the end of the world or being orphaned
        try:
            while True:
                if os.getppid() == 1:
                    log_debug("I am an orphan - stopping")
                    break
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

        java_spark_sql_session = gateway.entry_point.getSparkSQLSession()
        spark_version = spark_context.version
        spark_sql_session = None
        if spark_version == "1.6.1":
            from pyspark import HiveContext
            java_sql_context = java_spark_sql_session.getSQLContext()
            spark_sql_session = HiveContext(spark_context, java_sql_context)
        elif spark_version == "2.0.0":
            from pyspark.sql import SparkSession
            java_spark_session = java_spark_sql_session.getSparkSession()
            spark_sql_session = SparkSession(spark_context, java_spark_session)
        else:
            raise ValueError("Spark version {} is not supported".format(spark_version))

        return spark_context, spark_sql_session

    @staticmethod
    def _initialize_gateway(gateway_address):
        (host, port) = gateway_address

        callback_params = CallbackServerParameters(address=host, port=0)

        gateway = JavaGateway(GatewayClient(address=host, port=port),
                              start_callback_server=True,
                              auto_convert=True,
                              callback_server_parameters=callback_params)
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
