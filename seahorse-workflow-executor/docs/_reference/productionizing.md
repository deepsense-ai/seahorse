---
layout: global
displayTitle: Productionizing Workflows
menuTab: reference
title: Productionizing
description: Productionizing Workflows with Seahorse Batch Workflow Executor
---

**Table of Contents**

* Table of Contents
{:toc}

## Overview

Production-ready workflows can be exported as standalone
Apache Spark applications and executed on any cluster in a batch mode.

Seahorse Batch Workflow Executor is an Apache Spark
application that allows you to execute standalone workflows.
This functionality can facilitate integration of Seahorse with other data processing systems
and manage the execution of workflows outside of Seahorse Editor.

<div class="centered-container" markdown="1">
  ![Seahorse Batch Workflow Executor Overview](../img/batch_overview.png){: .centered-image .img-responsive}
  *Seahorse Batch Workflow Executor Overview*
</div>

## Get Seahorse Batch Workflow Executor

Seahorse Batch Workflow Executor is available in a form of both precompiled binaries and source code.

#### Use Precompiled Binaries

| **Seahorse Batch Workflow Executor Version** | **Apache Spark Version** | **Scala Version** | **Link** |
| 1.4.2 | 2.1.1 | 2.11 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.4.2/workflowexecutor_2.11-1.4.2.jar">download</a> |

#### Build from Source

If you are interested in compiling Seahorse Batch Workflow Executor from source
you can check out our Git repository:

```
git clone {{ site.WORKFLOW_EXECUTOR_GITHUB_URL }}
```


## How to Run Seahorse Batch Workflow Executor

Seahorse Batch Workflow Executor can be submitted to an Apache Spark cluster as any other Apache Spark application.
For more detailed information about submitting Apache Spark applications visit
<a target="_blank" href="{{ site.SPARK_DOCS }}/submitting-applications.html">{{ site.SPARK_DOCS }}/submitting-applications.html</a>

#### Local Apache Spark (single machine)
{% highlight bash %}
# Run Application Locally (on 8 cores)
./bin/spark-submit \
  --driver-class-path workflowexecutor.jar \
  --class ai.deepsense.workflowexecutor.WorkflowExecutorApp \
  --master local[8] \
  --files workflow.json \
  workflowexecutor.jar \
    --workflow-filename workflow.json \
    --output-directory test-output \
    --custom-code-executors-path workflowexecutor.jar
{% endhighlight %}

#### Apache Spark Standalone Cluster
{% highlight bash %}
# Run on Apache Spark Standalone Cluster in Client Deploy Mode
./bin/spark-submit \
  --driver-class-path workflowexecutor.jar \
  --class ai.deepsense.workflowexecutor.WorkflowExecutorApp \
  --master spark://207.184.161.138:7077 \
  --files workflow.json \
  workflowexecutor.jar \
    --workflow-filename workflow.json \
    --output-directory test-output \
    --custom-code-executors-path workflowexecutor.jar
{% endhighlight %}

#### YARN Cluster
{% highlight bash %}
# Run on YARN Cluster
export HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop   # location of Hadoop cluster configuration directory
./bin/spark-submit \
  --driver-class-path workflowexecutor.jar \
  --class ai.deepsense.workflowexecutor.WorkflowExecutorApp \
  --master yarn \
  --deploy-mode client \
  --files workflow.json \
  workflowexecutor.jar \
    --workflow-filename workflow.json \
    --output-directory test-output \
    --custom-code-executors-path workflowexecutor.jar
{% endhighlight %}

#### Mesos Cluster
{% highlight bash %}
# Run on Mesos Cluster
export LIBPROCESS_ADVERTISE_IP={user-machine-IP}   # IP addres of user's machine, visible from Mesos cluster
export LIBPROCESS_IP={user-machine-IP}   # IP addres of user's machine, visible from Mesos cluster
./bin/spark-submit \
  --driver-class-path workflowexecutor.jar \
  --class ai.deepsense.workflowexecutor.WorkflowExecutorApp \
  --master mesos://207.184.161.138:5050 \
  --deploy-mode client \
  --supervise \
  --files workflow.json \
  --conf spark.executor.uri=http://d3kbcqa49mib13.cloudfront.net/spark-2.0.2-bin-hadoop2.7.tgz \
  workflowexecutor.jar \
    --workflow-filename workflow.json \
    --output-directory test-output \
    --custom-code-executors-path workflowexecutor.jar
{% endhighlight %}

Option ``--custom-code-executors-path`` is required (`workflowexecutor.jar` contains PyExecutor and RExecutor).
Option ``--files workflow.json`` is necessary to distribute workflow file within the Apache Spark cluster.
It is necessary to pass the same filename to ``--workflow-filename workflow.json`` option,
in order to tell Seahorse Batch Workflow Executor under which name it should look for a workflow file.

If `{{ site.SPARK_ASSEMBLY_PATH }}` is already distributed
on HDFS cluster, it is possible to reduce the time necessary for files propagation on the YARN cluster. Use the `spark-submit` option
``--conf spark.yarn.jar=hdfs:///path/to/{{ site.SPARK_ASSEMBLY_PATH }}``
with a proper HDFS path.
Apache Spark assembly jar can be found in Apache Spark {{ site.WORKFLOW_EXECUTOR_SPARK_VERSION }}
compiled for Hadoop {{ site.WORKFLOW_EXECUTOR_HADOOP_VERSION }} package.

**NOTE:** Paths of files listed in the `--files` option cannot contain white or special characters.

## Custom JDBC Drivers

To allow usage of SQL databases for
[Read DataFrame](../operations/read_dataframe.html)
and
[Write DataFrame](../operations/write_dataframe.html),
a proper JDBC driver has to be accessible during workflow's execution.
This requirement can be satisfied by:

* adding the JDBC jar library to cluster deployment, or

* adding the JDBC jar to the driver's classpath during `spark-submit` command (`--jars` option).

To specify JDBC jar during execution, use `spark-submit`'s option
``--driver-class-path``, e.g. ``--driver-class-path "path/to/jdbc-driver1.jar:path/to/jdbc-driver2.jar:workflowexecutor.jar"``.
For more information, please visit
<a target="_blank" href="{{ site.SPARK_DOCS }}/configuration.html#runtime-environment">Apache Spark documentation</a>.

## Using SDK

To execute workflow containing user-defined operations (see: [SDK User Guide](sdk_user_guide.html)),
user has to specify jars containing those operation to be accessible during workflow's execution.
This requirement can be satisfied by:

* adding the SDK jar to cluster deployment, or

* adding the SDK jar to the driver's classpath during `spark-submit` command (`--jars` option).

To specify Seahorse SDK jar during execution, use `spark-submit`'s option
``--driver-class-path``, e.g. ``--driver-class-path "path/to/skd1.jar:path/to/sdk2.jar:workflowexecutor.jar"``.
For more information, please visit
<a target="_blank" href="{{ site.SPARK_DOCS }}/configuration.html#runtime-environment">Apache Spark documentation</a>.

## Seahorse Batch Workflow Executor Command Line Parameters

Detailed information about command line parameters can be obtained by executing command:

``java -classpath workflowexecutor.jar ai.deepsense.workflowexecutor.WorkflowExecutorApp --help``

#### Command Line Parameters Details

| Argument                                                        | Meaning |
|:----------------------------------------------------------------|:--------|
| ``-w FILENAME``<BR/>``--workflow-filename FILENAME``            | Workflow filename. If specified, workflow will be read from passed location. The file has to be accessible by the driver. |
| ``-o DIR``<BR/>``--output-directory DIR``                       | Output directory path. If specified, execution report will be saved to passed location. Directory will be created if it does not exist. |
| ``-e:NAME=VALUE``<BR/>``--extra-var:NAME=VALUE``                | Extra variable. Sets an extra variable to a specified value. Can be specified multiple times. |
| ``-x PATH``<BR/>``--custom-code-executors-path PATH``           | Custom code executors (included in workflowexecutor.jar) path. |
| ``--python-binary PATH``                                        | Python binary path. |
| ``-t PATH``<BR/>``--temp-dir PATH``                             | Temporary directory path. |

* **NOTE:** Parameter ``-w FILENAME`` (or its long name) needs to be specified.
* **NOTE:** Both parameters ``-w FILENAME`` and ``-o DIR`` (or their long names) have to be specified.
* **NOTE:** When using ``--extra-var`` option,
if variable name or value contains special characters (e.g. space),
it has to be surrounded by quotation marks (“”).


## Seahorse Batch Workflow Executor Logs

Depending on Apache Spark application deployment mode and cluster configuration, execution logs can be
redirected to several locations, e.g.:

* Submitter's console (running Apache Spark locally or when deploy mode is `client`)

* YARN logs directory on cluster nodes

* Apache Spark logs directory on cluster nodes

* HDFS directory

For detailed information about logging with regard to your cluster configuration,
for running Apache Spark on YARN, visit:
<a target="_blank" href="{{ site.SPARK_DOCS }}/running-on-yarn.html#debugging-your-application">{{ site.SPARK_DOCS }}/running-on-yarn.html#debugging-your-application</a>,
for Apache Spark Standalone cluster, visit:
<a target="_blank" href="{{ site.SPARK_DOCS }}/spark-standalone.html#monitoring-and-logging">{{ site.SPARK_DOCS }}/spark-standalone.html#monitoring-and-logging</a>.

For details on how Apache Spark runs on clusters, visit:
<a target="_blank" href="{{ site.SPARK_DOCS }}/cluster-overview.html">{{ site.SPARK_DOCS }}/cluster-overview.html</a>.
