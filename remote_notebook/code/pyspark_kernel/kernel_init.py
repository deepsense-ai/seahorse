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


from pyspark import SparkContext
from pyspark import SparkConf
from pyspark.sql import SQLContext
from pyspark.sql import DataFrame
from py4j.java_gateway import JavaGateway, GatewayClient, java_import
from py4j.protocol import Py4JJavaError

# gateway_address and gateway_port are set in the kernel
gateway = JavaGateway(
    GatewayClient(
        address=gateway_address,
        port=gateway_port),
    start_callback_server=False,
    auto_convert=True)

java_spark_context = gateway.entry_point.getSparkContext()
java_spark_conf = gateway.entry_point.getSparkConf()

java_import(gateway.jvm, "org.apache.spark.SparkEnv")
java_import(gateway.jvm, "org.apache.spark.SparkConf")
java_import(gateway.jvm, "org.apache.spark.api.java.*")
java_import(gateway.jvm, "org.apache.spark.api.python.*")
java_import(gateway.jvm, "org.apache.spark.mllib.api.python.*")
java_import(gateway.jvm, "org.apache.spark.sql.*")
java_import(gateway.jvm, "org.apache.spark.sql.hive.*")
java_import(gateway.jvm, "scala.Tuple2")
java_import(gateway.jvm, "scala.collection.immutable.List")

sc = SparkContext(
    conf=SparkConf(_jvm=gateway.jvm, _jconf=java_spark_conf),
    gateway=gateway,
    jsc=java_spark_context)


try:
  from pyspark.sql import SparkSession
  java_spark_sql_session = gateway.entry_point.getNewSparkSQLSession()
  java_spark_session = java_spark_sql_session.getSparkSession()
  spark = SparkSession(sc, java_spark_session)
  sqlContext = SQLContext(sc, spark)
except ImportError:
  sqlContext = SQLContext(sc)

def dataframe():
    # workflow_id, node_id and port_number are set in the kernel
    if node_id is None or port_number is None:
        raise Exception("No edge is connected to this Notebook")

    try:
        if dataframe_storage_type == 'output':
            java_data_frame = gateway.entry_point.retrieveOutputDataFrame(workflow_id, node_id, port_number)
        else:
            assert dataframe_storage_type == 'input'
            java_data_frame = gateway.entry_point.retrieveInputDataFrame(workflow_id, node_id, port_number)
    except Py4JJavaError:
        raise Exception("Input operation is not yet executed")

    return move_to_local_sqlContext(DataFrame(jdf=java_data_frame, sql_ctx=sqlContext))

def move_to_local_sqlContext(df):
    return sqlContext.createDataFrame(df.rdd, df.schema)
