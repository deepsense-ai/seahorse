# Copyright (c) 2016, CodiLime Inc.
FROM ubuntu:16.04

# Install packages
RUN apt-get update && \
    apt-get install -y openjdk-8-jre wget curl bzip2 openssh-server openssh-client libsm6 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Setup passwordless SSH
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa
RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

# Install Spark
ENV SPARK_VERSION 1.6.1
ENV SPARK_PACKAGE spark-$SPARK_VERSION-bin-hadoop2.6
ENV SPARK_HOME /opt/spark-$SPARK_VERSION
ENV PATH $PATH:$SPARK_HOME/bin
RUN wget -q -O - \
  "http://d3kbcqa49mib13.cloudfront.net/$SPARK_PACKAGE.tgz" \
  | gunzip \
  | tar x -C /tmp/ \
  && mv /tmp/$SPARK_PACKAGE $SPARK_HOME \
  && rm -rf $SPARK_HOME/examples $SPARK_HOME/ec2

ENTRYPOINT [ "/opt/startup.sh" ]
CMD [ "-d" ]
