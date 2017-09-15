# Copyright (c) 2015, CodiLime Inc.

import commons
import subprocess
from json_pattern_utils import match_report
from robot.libraries.BuiltIn import BuiltIn


class WorkflowExecutorClient(object):
  def __init__(self):
    super(WorkflowExecutorClient, self).__init__()
    conf = commons.confGetter('workflow-executor-client')
    self.workflow_executor_jar = conf('workflowExecutorJar')
    self.workflow_executor_class = conf('workflowExecutorClass')
    self.spark_master = conf('sparkMaster')

  def run_workflow(self, workflow_file_path=None, spark_master=None, spark_submit_options="",
                   output_path=None):
    if spark_master is None:
      spark_master = self.spark_master
    if workflow_file_path is None:
      workflow_file_path = get_workflow_path()
    if output_path is None:
      output_path = get_output_path()

    command = spark_submit_command(
      we_class=self.workflow_executor_class,
      spark_master=spark_master,
      spark_submit_options=spark_submit_options,
      jar=self.workflow_executor_jar,
      workflow_filename=workflow_file_path,
      output_dir=output_path
    )

    print command

    spark_submit = subprocess.Popen(
      command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    for line in spark_submit.stdout.readlines():
      print line,

    if spark_submit.wait() != 0:
      raise AssertionError('spark-submit failed.')

  def run_workflow_local(self, workflow_file_path=None, spark_submit_options=""):
    if workflow_file_path is None:
      workflow_file_path = get_workflow_path()
    self.run_workflow(workflow_file_path, "local[4]", spark_submit_options)

  def check_report(self, report_pattern_path=None, result_report_path=None):
    if report_pattern_path is None:
      report_pattern_path = get_expected_report_path()
    if result_report_path is None:
      result_report_path = get_result_report_path()
    match_report(report_pattern_path, result_report_path)


def spark_submit_command(**kwargs):
  return """spark-submit --class {we_class} --master {spark_master} --files {workflow_filename}\\
    --driver-class-path {jar} {spark_submit_options} {jar}\\
    --noninteractive-mode --python-executor-path ./python/pyexecutor/pyexecutor.py\\
    --workflow-filename {workflow_filename} --output-directory {output_dir}\\
    --report-level high""".format(
    we_class=kwargs['we_class'],
    spark_master=kwargs['spark_master'],
    jar=kwargs['jar'],
    workflow_filename=kwargs['workflow_filename'],
    output_dir=kwargs['output_dir'],
    spark_submit_options=kwargs['spark_submit_options'])


def get_output_path():
  return BuiltIn().get_variable_value("${OUTPUT PATH}")


def get_workflow_path():
  return BuiltIn().get_variable_value("${WORKFLOW PATH}")


def get_expected_report_path():
  return BuiltIn().get_variable_value("${EXPECTED REPORT PATH}")

def get_result_report_path():
  return BuiltIn().get_variable_value("${RESULT REPORT PATH}")
