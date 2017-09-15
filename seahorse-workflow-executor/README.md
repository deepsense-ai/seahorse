# Seahorse Workflow Executor

## Overview

Workflow Executor is an <a target="_blank" href="http://spark.apache.org">Apache Spark</a>
application that allows user to execute workflows created by <a target="_blank" href="https://seahorse.deepsense.ai"/>Seahorse Editor</a>.

Workflow Executor requires:

* Scala version: 2.11.8+

* Spark version: 2.0.0

* Hadoop version: 2.7.0

## Building Workflow Executor from Sources

Steps required to build Workflow Executor:

* <a target="_blank" href="http://www.scala-lang.org/download/install.html">Install Scala</a>

* <a target="_blank" href="http://www.scala-sbt.org/release/tutorial/Installing-sbt-on-Linux.html">Install sbt</a>

Execute the following commands:

    sbt workflowexecutor/assembly

Assembled jar can be found under path:

``workflowexecutor/target/scala-2.11/workflowexecutor.jar``

## How to Run Workflow Executor

Workflow Executor can be submitted to Spark cluster as any other Spark application.
Example spark-submit commands can be found in following subsections.
Replace `./bin/spark-submit` with path to script in Apache Spark's directory.
For more detailed information about submitting Spark applications, visit:
<a target="_blank" href="{{site.SPARK_DOCS}}/submitting-applications.html">{{site.SPARK_DOCS}}/submitting-applications.html</a>

#### Local (single machine) Spark
    # Run application locally (on 8 cores)
    ./bin/spark-submit \
      --driver-class-path workflowexecutor.jar \
      --class ai.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master local[8] \
      --files workflow.json \
      workflowexecutor.jar \
        --workflow-filename workflow.json \
        --output-directory test-output \
        --python-executor-path workflowexecutor.jar

#### Spark Standalone Cluster
    # Run on a Spark Standalone cluster in client deploy mode
    ./bin/spark-submit \
      --driver-class-path workflowexecutor.jar \
      --class ai.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master spark://207.184.161.138:7077 \
      --files workflow.json \
      workflowexecutor.jar \
        --workflow-filename workflow.json \
        --output-directory test-output \
        --python-executor-path workflowexecutor.jar

#### YARN Cluster
    # Run on a YARN cluster
    export HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop   # location of Hadoop cluster configuration directory
    ./bin/spark-submit \
      --driver-class-path workflowexecutor.jar \
      --class ai.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master yarn-cluster \  # can also be `yarn-client` for client mode
      --files workflow.json \
      workflowexecutor.jar \
        --workflow-filename workflow.json \
        --output-directory test-output \
        --python-executor-path workflowexecutor.jar

Option ``--files workflow.json`` is necessary to distribute workflow file to Spark cluster.
It is necessary to pass the same filename to ``--workflow-filename workflow.json`` option,
in order to tell Workflow Executor under which name it should look for workflow file.

If spark-assembly-2.0.0-hadoop2.7.0.jar is already distributed
on HDFS cluster, it is possible to reduce time necessary for files propagation on YARN cluster.
Use spark-submit option
``--conf spark.yarn.jar=hdfs:///path/to/spark-assembly-2.0.0-hadoop2.7.0.jar``
with proper HDFS path.
Spark assembly jar can be found in Spark 2.0.0 compiled for
Hadoop 2.7.0 package (Seahorse uses Scala 2.11, Spark has to be built with Scala 2.11 support).



## Workflow Executor Command Line Parameters

Detailed information about command line parameters can be obtained by executing command:

``java -classpath workflowexecutor.jar ai.deepsense.workflowexecutor.WorkflowExecutorApp --help``

#### Command Line Parameters Details

| Argument                                                        | Meaning |
|:----------------------------------------------------------------|:--------|
| ``-w FILE``<BR/>``--workflow-filename FILE``                    | Workflow filename. If specified, workflow will be read from passed location. The file has to be accessible by the driver. |
| ``-o DIR``<BR/>``--output-directory DIR``                       | Output directory path. If specified, execution report will be saved to passed location. Directory will be created if it does not exist. |
| ``-e NAME=VALUE``<BR/>``--extra-var NAME=VALUE``                | Extra variable. Sets extra variable to specified value. Can be specified multiple times. |
| ``-m HOST``<BR/>``--message-queue-host HOST``                   | Address of message queue host. |
| ``-p PATH``<BR/>``--python-executor-path PATH``                 | Path to PyExecutor code (included in workflowexecutor.jar). |


## Workflow Executor Logs

Depending on Spark application deployment mode and cluster configuration, execution logs can be
redirected to several locations, e.g.:

* Submitter's console (running Spark locally or when deploy mode is `client`)

* YARN logs directory on cluster nodes

* Spark logs directory on cluster nodes

* HDFS directory

You have to look for detailed information about logging with regard to Your cluster configuration,
for running Spark on YARN, visit:
<a target="_blank" href="{{site.SPARK_DOCS}}/running-on-yarn.html#debugging-your-application">{{site.SPARK_DOCS}}/running-on-yarn.html#debugging-your-application</a>,
for Spark Standalone cluster, visit:
<a target="_blank" href="{{site.SPARK_DOCS}}/spark-standalone.html#monitoring-and-logging">{{site.SPARK_DOCS}}/spark-standalone.html#monitoring-and-logging</a>.

For details on how Spark runs on clusters, visit:
<a target="_blank" href="{{site.SPARK_DOCS}}/cluster-overview.html">{{site.SPARK_DOCS}}/cluster-overview.html</a>.
