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

# Install conda
RUN wget -q -O /tmp/Miniconda2-latest-Linux-x86_64.sh \
    https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh && \
    bash /tmp/Miniconda2-latest-Linux-x86_64.sh -f -b -p /opt/conda && \
    rm /tmp/Miniconda2-latest-Linux-x86_64.sh

# Install python packages
RUN /opt/conda/bin/conda install --yes notebook terminado \
    'ipython=4.0*' 'ipywidgets=4.0*' 'pandas=0.16*' 'matplotlib=1.4*' 'scipy=0.15*' 'seaborn=0.6*' 'scikit-learn=0.16*' pyzmq

COPY startup.sh /opt/
RUN chown root.root /opt/startup.sh && chmod 700 /opt/startup.sh

ENTRYPOINT [ "/opt/startup.sh" ]
CMD [ "-d" ]
