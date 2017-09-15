# Copyright (c) 2015, CodiLime Inc.

# Example configuration of using WorkflowManager notebook storage

from wmcontents import WMContentsManager

c.NotebookApp.contents_manager_class = WMContentsManager
c.WMContentsManager.workflow_manager_url = "http://172.28.128.1:9080"
c.WMContentsManager.kernel_name = "pyspark"
c.WMContentsManager.kernel_display_name = "Python (Spark)"
c.WMContentsManager.kernel_python_version = "2.7.10"
