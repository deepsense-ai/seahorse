# Copyright (c) 2015, CodiLime Inc.

import commons
import json
import os
import shutil
import subprocess
import time

class WorkflowExecutorClient(object):
  def __init__(self):
    super(WorkflowExecutorClient, self).__init__()
    conf = commons.confGetter('workflow-executor-client')
    self.workflow_executor_jar = conf('workflowExecutorJar')
    self.workflow_executor_class = conf('workflowExecutorClass')
    self.spark_master = conf('sparkMaster')
    self.output_dir = "test-output"

  def run_workflow(self, workflow_file_path, spark_master=None, spark_submit_options=None):
    if spark_master == None:
      spark_master = self.spark_master

    command = spark_submit_command(
      we_class=self.workflow_executor_class,
      spark_master=spark_master,
      spark_submit_options=spark_submit_options,
      jar=self.workflow_executor_jar,
      workflow_filename=workflow_file_path,
      output_dir=self.output_dir)

    print command

    spark_submit = subprocess.Popen(
      command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    for line in spark_submit.stdout.readlines():
      print line,

    if spark_submit.wait() != 0:
      raise AssertionError('spark-submit failed.')

  def run_workflow_local(self, workflow_file_path, spark_submit_options=None):
    self.run_workflow(workflow_file_path, "local[4]", spark_submit_options)

  def check_execution_status(self, expected_status="COMPLETED"):
    actual_json = load_json(self.output_dir + "/result.json")
    if not 'executionReport' in actual_json.keys():
      raise AssertionError('Section executionReport not found.')
    execution_status = actual_json['executionReport']['status']
    if execution_status != expected_status:
      raise AssertionError(
        "Execution status = {status}, expected {expected}.".format(
          status=execution_status,
          expected=expected_status))

  def check_report(self, report_pattern_path):
    json_result = load_json(self.output_dir + "/result.json")
    json_pattern = load_json(report_pattern_path)

    for node_id in json_pattern.keys():
      entity_ids = json_result['executionReport']['nodes'][node_id]['results']
      actual_entity_reports = [json_result['executionReport']['resultEntities'][entity_id]
        for entity_id in entity_ids]
      expected_entity_reports = json_pattern[node_id]
      if len(actual_entity_reports) != len(expected_entity_reports):
        raise AssertionError('Actual report contains unexpected number of entities.')
      zipped_reports = zip(actual_entity_reports, expected_entity_reports)
      for (actual_entity_report, expected_entity_report) in zipped_reports:
        try:
          order_ignorant_expected = json_lists_to_sets(expected_entity_report)
          order_ignorant_actual = json_lists_to_sets(actual_entity_report)
          check_json_containment(order_ignorant_expected, order_ignorant_actual)
        except Exception as e:
          raise AssertionError('Actual report does not match pattern. ' + str(e))

  def check_error(self, error_pattern_path):
    actual_error = load_json(self.output_dir + "/result.json")['executionReport']
    error_pattern = load_json(error_pattern_path)
    order_ignorant_actual = json_lists_to_sets(actual_error)
    order_ignorant_expected = json_lists_to_sets(error_pattern)
    try:
      check_json_containment(order_ignorant_expected, order_ignorant_actual)
    except Exception as e:
      raise AssertionError('Actual error report does not match pattern. ' + str(e))

  def create_output_directory(self, output_dir_name):
    self.output_dir = output_dir_name
    if not os.path.exists(output_dir_name):
      os.mkdir(output_dir_name)

  def clean_output_directory(self):
    shutil.rmtree(self.output_dir)

def spark_submit_command(**kwargs):
  return """spark-submit --class {we_class} --master {spark_master} --files {workflow_filename}\\
    {spark_submit_options} {jar}\\
    --workflow-filename {workflow_filename} --output-directory {output_dir}\\
    --report-level high""".format(
    we_class=kwargs['we_class'],
    spark_master=kwargs['spark_master'],
    jar=kwargs['jar'],
    workflow_filename=kwargs['workflow_filename'],
    output_dir=kwargs['output_dir'],
    spark_submit_options=kwargs['spark_submit_options'])

def load_json(filename):
  json_file = open(filename, 'r')
  json_content = json.loads(json_file.read())
  json_file.close()
  return json_content

def check_json_containment(contained, container):
  """ `contained` is contained by `container` if all keys from `contained`        """
  """ are present in `container` and their values from `contained` are contained  """
  """ in corresponding values from `container`. For arrays, strings and numerics  """
  """ containment is equivalent to equality.                                      """
  if type(contained) != type(container):
    raise Exception(str(contained) + " and " + str(container) + " don't have the same type")
  elif type(contained) == dict:
    contained_key_set = set(contained.keys())
    container_key_set = set(container.keys())
    if not contained_key_set.issubset(container_key_set):
      raise Exception("{ " + ", ".join(container.keys()) +
        " } does not have all keys from { " + ", ".join(contained.keys()) + " }")
    for key in contained.keys():
      check_json_containment(contained[key], container[key])
  else:
    if contained != container:
      raise Exception(str(contained) + " != " + str(container))

def json_lists_to_sets(json_value):
  if type(json_value) == dict:
    return { k: json_lists_to_sets(v) for k, v in json_value.items() }
  elif type(json_value) == list:
    return frozenset(json_lists_to_sets(item) for item in json_value)
  else:
    return json_value
