/**
 * Copyright (c) 2015, CodiLime Inc.
 */

To build WorkflowExecutor, run:
sbt clean workflowexecutor/assembly


Now You can run WorkflowExecutor on Spark standalone cluster:
/opt/spark/bin/spark-submit \
  --class io.deepsense.workflowexecutor.WorkflowExecutor \
  --master spark://ds-gzes-env-master:7077 \
  workflowexecutor/target/deepsense-workflowexecutor-assembly-0.1-SNAPSHOT.jar \
  1

Or on YARN on-demand-Spark-cluster (results can be found on: http://ds-gzes-env-master:8088):
export HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop
/opt/spark/bin/spark-submit \
  --class io.deepsense.workflowexecutor.WorkflowExecutor \
  --master yarn-cluster \
  workflowexecutor/target/deepsense-workflowexecutor-assembly-0.1-SNAPSHOT.jar \
  1


NOTE: You can redirect Spark logs by appending to run commands:
 2> /dev/null

To avoid uploading spark-assembly.jar to cluster, use spark-assembly.jar stored in HDFS (You have to deploy it manualy), use spark-submit option:
  --conf spark.yarn.jar=hdfs:///deepsense/lib/spark-assembly-1.4.0-hadoop2.6.0.jar \

To include files (experiment.json in example) necessary for execution on cluster, use spark-submit option:
  --jars experiment.json \

