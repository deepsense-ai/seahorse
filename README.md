# Seahorse Workflow Executor

## Overview

Workflow Executor is an <a target="_blank" href="http://spark.apache.org">Apache Spark</a>
application that allows user to execute <a target="_blank" href="http://seahorse.deepsense.io/docs/latest/workflowfile.html">workflows</a> created by <a target="_blank" href="http://editor.seahorse.deepsense.io"/>Seahorse Editor</a>.

Workflow Executor requires:

* Scala version: 2.10.+

* Spark version: 1.4.0

* Hadoop version: 2.6.0

## Building Workflow Executor from sources

Steps required to build Workflow Executor:

* <a target="_blank" href="http://www.scala-lang.org/download/install.html">Install Scala</a>

* <a target="_blank" href="http://www.scala-sbt.org/release/tutorial/Installing-sbt-on-Linux.html">Install sbt</a>

Execute the following commands:

    sbt workflowexecutor/assembly

Assembled jar can be found under path:

``workflowexecutor/target/scala-2.10/workflowexecutor.jar``

## How to run Workflow Executor

Workflow Executor can be submitted to Spark cluster as any other Spark application.
Example spark-submit commands can be found in following subsections.
Replace `./bin/spark-submit` with path to script in Apache Spark's directory.
For more detailed information about submitting Spark applications, visit:
<a target="_blank" href="https://spark.apache.org/docs/1.4.0/submitting-applications.html">https://spark.apache.org/docs/1.4.0/submitting-applications.html</a>

#### Local (single machine) Spark
    # Run application locally (on 8 cores)
    ./bin/spark-submit \
      --class io.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master local[8] \
      --files workflow.json \
      workflowexecutor.jar \
        --workflow-filename workflow.json \
        --output-directory test-output \
        --report-level medium

#### Spark Standalone cluster
    # Run on a Spark Standalone cluster in client deploy mode
    ./bin/spark-submit \
      --class io.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master spark://207.184.161.138:7077 \
      --files workflow.json \
      workflowexecutor.jar \
        --workflow-filename workflow.json \
        --output-directory test-output \
        --report-level medium

#### YARN cluster
    # Run on a YARN cluster
    export HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop   # location of Hadoop cluster configuration directory
    ./bin/spark-submit \
      --class io.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master yarn-cluster \  # can also be `yarn-client` for client mode
      --files workflow.json \
      workflowexecutor.jar \
        --workflow-filename workflow.json \
        --output-directory test-output \
        --report-level medium

Option ``--files workflow.json`` is necessary to distribute workflow file to Spark cluster.
It is necessary to pass the same filename to ``--workflow-filename workflow.json`` option,
in order to tell Workflow Executor under which name it should look for workflow file.

If spark-assembly-1.4.0-hadoop2.6.0.jar is already distributed
on HDFS cluster, it is possible to reduce time necessary for files propagation on YARN cluster.
Use spark-submit option
``--conf spark.yarn.jar=hdfs:///path/to/spark-assembly-1.4.0-hadoop2.6.0.jar``
with proper HDFS path.
Spark assembly jar can be found in Spark 1.4.0 compiled for
Hadoop 2.6.0 package (Seahorse uses Scala 2.10, Spark has to be built with Scala 2.10 support).



## Workflow Executor Command Line Parameters

Detailed information about command line parameters can be obtained by executing command:

``java -classpath workflowexecutor.jar io.deepsense.workflowexecutor.WorkflowExecutorApp --help``

#### Command line parameters details

| Argument                                   | Meaning                                              |
|:-------------------------------------------|:-----------------------------------------------------|
| ``-r LEVEL``, ``--report-level LEVEL``     | Level of details for DataFrame report generation; LEVEL is 'high', 'medium', or 'low' (default: 'medium') |
|``-w FILE``, ``--workflow-filename FILE``   | Workflow filename                                    |
|``-d ID``, ``--download-workflow ID``       | Download workflow; if specified, workflow with passed ID will be downloaded from Seahorse Editor |
|``-o DIR``, ``--output-directory DIR``      | Output directory path; directory will be created if it does not exists; execution fails if any file is to be overwritten |
| ``-u``, ``--upload-report``                | Upload execution report; if specified, POST request with execution report will be sent after completing workflow execution |
| ``-e HOST``, ``--editor-host HOST``        | Hostname or IP of Seahorse Editor (optional)  |



## Workflow Executor Logs

Depending on Spark application deployment mode and cluster configuration, execution logs can be
redirected to several locations, e.g.:

* Submitter's console (running Spark locally or when deploy mode is `client`)

* YARN logs directory on cluster nodes

* Spark logs directory on cluster nodes

* HDFS directory

You have to look for detailed information about logging with regard to Your cluster configuration,
for running Spark on YARN, visit:
<a target="_blank" href="http://spark.apache.org/docs/1.4.0/running-on-yarn.html#debugging-your-application">http://spark.apache.org/docs/1.4.0/running-on-yarn.html#debugging-your-application</a>,
for Spark Standalone cluster, visit:
<a target="_blank" href="http://spark.apache.org/docs/1.4.0/spark-standalone.html#monitoring-and-logging">http://spark.apache.org/docs/1.4.0/spark-standalone.html#monitoring-and-logging</a>.

For details on how Spark runs on clusters, visit:
<a target="_blank" href="http://spark.apache.org/docs/1.4.0/cluster-overview.html">http://spark.apache.org/docs/1.4.0/cluster-overview.html</a>.
