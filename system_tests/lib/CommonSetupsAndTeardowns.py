# Copyright (c) 2015, CodiLime Inc.

import os
import shutil
from robot.libraries.BuiltIn import BuiltIn


class CommonSetupsAndTeardowns(object):
  def __init__(self):
    super(CommonSetupsAndTeardowns, self).__init__()

  # Suites commons
  def set_suite_variables(self):
    suite_name = BuiltIn().get_variable_value("${SUITE}")
    BuiltIn().set_suite_variable("${CLEAR OUTPUT}", "FALSE")
    self.clear_output = False
    BuiltIn().set_suite_variable("${SUITE PATH}", suite_name + "/")
    self.suite_path = suite_name + "/"
    BuiltIn().set_suite_variable("${SUITE RESOURCE PATH}", "resources/" + suite_name + "/")
    self.suite_resources_path = "resources/" + suite_name + "/"

  def standard_suite_setup(self):
    self.set_suite_variables()

  def standard_suite_teardown(self):
    pass

  # Tests commons
  def set_test_variables(self):
    cc_test_name = to_camel_case(BuiltIn().get_variable_value("${TEST NAME}"))
    suite_path = BuiltIn().get_variable_value("${SUITE PATH}")
    suite_resource_path = BuiltIn().get_variable_value("${SUITE RESOURCE PATH}")
    test_path = suite_path + cc_test_name + "/"
    test_resource_path = suite_resource_path + cc_test_name + "/"
    workflow_path = test_resource_path + "workflow.json"
    expected_report_path = test_resource_path + "expectedReportPattern.json"
    output_path = "output/" + test_path
    BuiltIn().set_test_variable("${TEST PATH}", test_path)
    self.test_path = test_path
    BuiltIn().set_test_variable("${TEST RESOURCE PATH}", test_resource_path)
    self.test_resource_path = test_resource_path
    BuiltIn().set_test_variable("${OUTPUT PATH}", output_path)
    self.output_path = output_path
    BuiltIn().set_test_variable("${WORKFLOW PATH}", workflow_path)
    self.workflow_path = workflow_path
    BuiltIn().set_test_variable("${EXPECTED REPORT PATH}", expected_report_path)
    self.expected_report_path = expected_report_path

  def standard_test_setup(self):
    self.set_test_variables()
    shutil.rmtree(self.output_path, ignore_errors=True)
    self.create_output_directory(self.output_path)

  def standard_test_teardown(self):
    self.clean_output_directory()

  # HDFS test commons
  def set_hdfs_test_variables(self):
    cc_test_name = to_camel_case(BuiltIn().get_variable_value("${TEST NAME}"))
    hdfs_path = "/system_tests/" + cc_test_name + "/"
    BuiltIn().set_test_variable("${HDFS PATH}", hdfs_path)
    self.hdfs_path = hdfs_path

  def standard_hdfs_test_setup(self):
    self.standard_test_setup()
    self.set_hdfs_test_variables()
    BuiltIn().run_keyword("Remove Hdfs Path", self.hdfs_path)

  def standard_hdfs_test_teardown(self):
    BuiltIn().run_keyword("Remove Hdfs Path", self.hdfs_path)
    self.standard_test_teardown()

  # S3 test Commons
  def set_s3_test_variables(self):
    cc_test_name = to_camel_case(BuiltIn().get_variable_value("${TEST NAME}"))
    s3_path = "system_tests/" + cc_test_name + "/"
    BuiltIn().set_test_variable("${S3 PATH}", s3_path)
    self.s3_path = s3_path

  def standard_s3_test_setup(self):
    self.standard_test_setup()
    self.set_s3_test_variables()
    BuiltIn().run_keyword("Remove From S3", self.s3_path)

  def standard_s3_test_teardown(self):
    BuiltIn().run_keyword("Remove from S3", self.s3_path)
    self.standard_test_teardown()

  def create_output_directory(self, output_dir_name=None):
    if output_dir_name is None:
      output_dir_name = self.output_path
    if not os.path.exists(output_dir_name):
      os.makedirs(output_dir_name)

  def clean_output_directory(self):
    clear_output = BuiltIn().get_variable_value("${CLEAR OUTPUT}")
    if clear_output != "FALSE":
      shutil.rmtree(self.output_path)


def to_camel_case(string):
  pascal_case = ''.join(x for x in string.title() if not x.isspace())
  camel_case = pascal_case[0].lower() + pascal_case[1:]
  return camel_case
