..
  Copyright 2015, CodiLime Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

Submitting Workflow
===================

--------------------
Building from source
--------------------
To prepare single jar file with WorkflowExecutor and all its dependencies, use command:

``sbt workflowexecutor/assembly``

Assembled jar can be found in:

``workflowexecutor/target/workflowexecutor.jar``


------------
Dependencies
------------
WorkflowExecutor requires Spark 1.4.0 for submitting workflows.
In following examples we assume Spark 1.4.0 is installed in /opt/spark directory.


----------------------------
Submitting workflow to Spark
----------------------------
WorkflowExecutor can be submitted as any other Spark application.
Working examples are presented in following subsections.
For more detailed information about submitting Spark Applications, visit:
https://spark.apache.org/docs/1.4.0/submitting-applications.html

NOTE: Option ``--jars workflow.json`` is necessary to pass workflow to WorkflowExecution.
Workflow description file has to be named ``workflow.json``.


Local (single machine) Spark
----------------------------
.. code-block:: bash

   # Run application locally (on 8 cores)
   /opt/spark/bin/spark-submit \
     --class io.deepsense.workflowexecutor.WorkflowExecutor \
     --master local[8] \
     --jars workflow.json \
     workflowexecutor/target/workflowexecutor.jar \
     10

Spark Standalone cluster
------------------------
.. code-block:: bash

   # Run on a Spark Standalone cluster in client deploy mode
   /opt/spark/bin/spark-submit \
     --class io.deepsense.workflowexecutor.WorkflowExecutor \
     --master spark://207.184.161.138:7077 \
     --jars workflow.json \
     workflowexecutor/target/workflowexecutor.jar \
     10

YARN cluster
------------
.. code-block:: bash

   # Run on a YARN cluster
   export HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop   # location of Hadoop cluster configuration directory
   /opt/spark/bin/spark-submit \
     --class io.deepsense.workflowexecutor.WorkflowExecutor \
     --master yarn-cluster \  # can also be `yarn-client` for client mode
     --jars workflow.json \
     workflowexecutor/target/workflowexecutor.jar \
     10

If spark-assembly-1.4.0-hadoop2.6.0.jar is already distributed on HDFS cluster, it is possible to reduce time necessary for files propagation on YARN cluster.
Use option ``--conf spark.yarn.jar=hdfs:///path/to/spark-assembly-1.4.0-hadoop2.6.0.jar`` with proper HDFS path.
Spark assembly jar can be found in Spark 1.4.0 compiled for Hadoop 2.6.0 package
(DeepSense.io uses Scala 2.11, it might be necessary to build Spark from sources with Scala 2.11 support).


----------------------
Workflow file exapmple
----------------------
Example ``workflow.json`` content:

.. code-block:: json

   {
     "id": "652a1538-a84f-44d9-ad3a-9029e542bf54"
   }

.. _language_label:

