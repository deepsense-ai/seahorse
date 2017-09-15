#!/bin/bash

echo 'export PATH="/opt/conda/bin:$PATH"' >> $SPARK_HOME/conf/spark-env.sh
echo 'export PYSPARK_PYTHON="/opt/conda/bin/python"' >> $SPARK_HOME/conf/spark-env.sh
echo 'export PYSPARK_DRIVER_PYTHON="/opt/conda/bin/python"' >> $SPARK_HOME/conf/spark-env.sh
echo 'export SPARK_WORKER_INSTANCES="4"' >> $SPARK_HOME/conf/spark-env.sh
echo 'export SPARK_MASTER_OPTS="-Dspark.deploy.defaultCores=1"' >> $SPARK_HOME/conf/spark-env.sh

service ssh restart

$SPARK_HOME/sbin/start-all.sh

# Docker ready checkers might hook to `SPARK_DOCKER_READY` string from log output
echo 'SPARK_DOCKER_READY'

if [ "$1" == "-d" ] ; then
  while [ 1 == 1 ] ; do
    sleep 1
  done
else
  exec "$@"
fi
