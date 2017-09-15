# Copyright (c) 2015, CodiLime Inc.

import ast

from threading import Thread

from pyspark.sql.dataframe import DataFrame


class CodeExecutor(object):
    """
    This class handles code execution requests from Session Executor.
    """

    TRANSFORM_FUNCTION_NAME = 'transform'
    TRANSFORM_FUNCTION_ARITIES = [1]

    def __init__(self, spark_context, sql_context, entry_point):
        self.entry_point = entry_point
        self.spark_context = spark_context
        self.sql_context = sql_context

        self.threads = []

    def run(self, workflow_id, node_id, custom_operation_code):
        executor_thread = Thread(
            target=lambda: self._supervised_execution(workflow_id, node_id, custom_operation_code),
            name='Supervisor {}'.format(node_id))

        self.threads.append(executor_thread)

        executor_thread.daemon = True
        executor_thread.start()

    def _supervised_execution(self, workflow_id, node_id, custom_operation_code):
        try:
            self._run_custom_code(workflow_id, node_id, custom_operation_code)
        finally:
            self.entry_point.executionFinished(workflow_id, node_id)

    def _run_custom_code(self, workflow_id, node_id, custom_operation_code):
        """
        :param workflow_id:
        :param node_id: id of node of the DOperation associated with the custom code
        :param custom_operation_code: The code is expected to include a top-level definition
        of a function named according to TRANSFORM_FUNCTION_NAME value
        :return: None
        """

        # This should've been checked before running
        assert self.validate(custom_operation_code)

        input_data_frame = DataFrame(
            jdf=self.entry_point.retrieveInputDataFrame(workflow_id, node_id),
            sql_ctx=self.sql_context)

        # noinspection PyBroadException
        try:
            context = {
                'sc': self.spark_context,
                'sqlContext': self.sql_context
            }

            exec custom_operation_code in context

            output_data_frame = context[self.TRANSFORM_FUNCTION_NAME](input_data_frame)

        except Exception as e:
            print 'Exception when running user-defined code: {}'.format(e)
            return

        if isinstance(output_data_frame, DataFrame):
            # noinspection PyProtectedMember
            self.entry_point.registerOutputDataFrame(workflow_id, node_id, output_data_frame._jdf)

    def validate(self, custom_operation_code):
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
        implements = ['io.deepsense.deeplang.PythonCodeExecutor']
