# Copyright 2015, deepsense.ai
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


import ast
import traceback
from pyspark import SQLContext
from pyspark.sql.dataframe import DataFrame
from threading import Thread

class CodeExecutor(object):
    """
    This class handles code execution requests from Session Executor.
    """

    TRANSFORM_FUNCTION_NAME = 'transform'
    TRANSFORM_FUNCTION_ARITIES = [1]

    INPUT_PORT_NUMBER = 0
    OUTPUT_PORT_NUMBER = 0

    def __init__(self, spark_context, spark_sql_session, entry_point):
        self.entry_point = entry_point
        self.spark_context = spark_context
        self.spark_sql_session = spark_sql_session

        self.threads = []

    def run(self, workflow_id, node_id, custom_operation_code):
        executor_thread = Thread(
            target=lambda: self._supervised_execution(workflow_id, node_id, custom_operation_code),
            name='Supervisor {}'.format(node_id))

        self.threads.append(executor_thread)

        executor_thread.daemon = True
        executor_thread.start()

    def _supervised_execution(self, workflow_id, node_id, custom_operation_code):
        # noinspection PyBroadException
        try:
            self._run_custom_code(workflow_id, node_id, custom_operation_code)
            self.entry_point.executionCompleted(workflow_id, node_id)
        except Exception as e:
            stacktrace = traceback.format_exc(e)
            self.entry_point.executionFailed(workflow_id, node_id, stacktrace)

    def _convert_data_to_data_frame(self, data):
        spark_sql_session = self.spark_sql_session
        sc = self.spark_context
        try:
            import pandas
            self.is_pandas_available = True
        except ImportError:
            self.is_pandas_available = False
        if isinstance(data, DataFrame):
            return data
        elif self.is_pandas_available and isinstance(data, pandas.DataFrame):
            return spark_sql_session.createDataFrame(data)
        elif isinstance(data, (list, tuple)) and all(isinstance(el, (list, tuple)) for el in data):
            return spark_sql_session.createDataFrame(sc.parallelize(data))
        elif isinstance(data, (list, tuple)):
            return spark_sql_session.createDataFrame(sc.parallelize(map(lambda x: (x,), data)))
        else:
            return spark_sql_session.createDataFrame(sc.parallelize([(data,)]))

    def _run_custom_code(self, workflow_id, node_id, custom_operation_code):
        """
        :param workflow_id:
        :param node_id: id of node of the DOperation associated with the custom code
        :param custom_operation_code: The code is expected to include a top-level definition
        of a function named according to TRANSFORM_FUNCTION_NAME value
        :return: None
        """

        # This should've been checked before running
        assert self.isValid(custom_operation_code)

        new_spark_session = self.spark_sql_session.newSession()

        new_sql_context = None
        spark_version = self.spark_context.version
        if spark_version in ["2.0.0", "2.0.1", "2.0.2", "2.1.0", "2.1.1"]:
            new_sql_context = SQLContext(self.spark_context, new_spark_session)
        elif spark_version == "1.6.1":
            new_sql_context = new_spark_session
        else:
            raise ValueError("Spark version {} is not supported".format(spark_version))

        raw_input_data_frame = DataFrame(
            jdf=self.entry_point.retrieveInputDataFrame(workflow_id,
                                                        node_id,
                                                        CodeExecutor.INPUT_PORT_NUMBER),
            sql_ctx=new_sql_context)
        input_data_frame = new_spark_session.createDataFrame(raw_input_data_frame.rdd)

        context = {
            'sc': self.spark_context,
            'spark': new_spark_session,
            'sqlContext': new_sql_context
        }

        exec custom_operation_code in context

        output_data = context[self.TRANSFORM_FUNCTION_NAME](input_data_frame)
        try:
            output_data_frame = self._convert_data_to_data_frame(output_data)
        except:
            raise Exception('Operation returned {} instead of a DataFrame'.format(output_data) + \
                ' (or pandas.DataFrame, single value, tuple/list of single values,' + \
                ' tuple/list of tuples/lists of single values) (pandas library available: ' + \
                str(self.is_pandas_available) + ').')

        # noinspection PyProtectedMember
        self.entry_point.registerOutputDataFrame(workflow_id,
                                                 node_id,
                                                 CodeExecutor.OUTPUT_PORT_NUMBER,
                                                 output_data_frame._jdf)

    # noinspection PyPep8Naming
    def isValid(self, custom_operation_code):
        def is_transform_function(field):
            return (isinstance(field, ast.FunctionDef) and
                    field.name == self.TRANSFORM_FUNCTION_NAME and
                    len(field.args.args) in self.TRANSFORM_FUNCTION_ARITIES)

        try:
            parsed = ast.parse(custom_operation_code)
        except SyntaxError:
            return False

        return any(filter(is_transform_function, parsed.body))

    # noinspection PyClassHasNoInit
    class Java:
        implements = ['ai.deepsense.deeplang.CustomCodeExecutor']
