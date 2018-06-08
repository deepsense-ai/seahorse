# Copyright (c) 2016, CodiLime Inc.

FROM ubuntu:16.04
MAINTAINER DeepSense.io

# To suppress some warnings
ENV TERM=xterm
ENV HADOOP_VERSION=hadoop-2.7.6
ENV JAVA_HOME=/usr/lib/jvm/java-8-oracle/jre/
ENV HADOOP_PREFIX=/opt/$HADOOP_VERSION
ENV HADOOP_HOME=$HADOOP_PREFIX
ENV HADOOP_COMMON_HOME=$HADOOP_PREFIX
ENV HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop
ENV HADOOP_HDFS_HOME=$HADOOP_PREFIX
ENV HADOOP_MAPRED_HOME=$HADOOP_PREFIX
ENV HADOOP_YARN_HOME=$HADOOP_PREFIX
ENV PATH=$HADOOP_PREFIX/bin:/opt:$PATH
ENV HDFS_USER=hdfs
ENV PATH $PATH:/opt/conda/bin

# Install additional tools
RUN apt-get update && apt-get install -y sudo htop bzip2

# Install Java
RUN apt-get update && apt-get install -y --no-install-recommends software-properties-common python-software-properties ca-certificates curl
RUN add-apt-repository ppa:webupd8team/java -y
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
RUN apt-get update && apt-get install -y oracle-java8-installer oracle-java8-set-default
RUN rm -rf /var/lib/apt/lists/*

# Install Hadoop
RUN curl -s ftp://ftp.task.gda.pl/pub/www/apache/dist/hadoop/common/$HADOOP_VERSION/$HADOOP_VERSION.tar.gz | tar -xz -C /opt/
RUN mkdir /data
RUN mkdir /data/logs
RUN mkdir /data/NameNode
RUN useradd -d /data -s /bin/bash hdfs
RUN chown -R hdfs:hdfs /data
RUN rm -fR /opt/$HADOOP_VERSION/logs
RUN ln -s /data/logs /opt/$HADOOP_VERSION/logs
COPY hadoop-conf/core-site.xml /opt/$HADOOP_VERSION/etc/hadoop/core-site.xml
COPY hadoop-conf/hdfs-site.xml /opt/$HADOOP_VERSION/etc/hadoop/hdfs-site.xml
COPY hadoop-conf/yarn-site.xml /opt/$HADOOP_VERSION/etc/hadoop/yarn-site.xml
COPY hadoop-conf/yarn-env.sh /opt/$HADOOP_VERSION/etc/hadoop/yarn-env.sh

# Install conda
RUN wget -q -O /tmp/Miniconda2-latest-Linux-x86_64.sh \
    https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh && \
    bash /tmp/Miniconda2-latest-Linux-x86_64.sh -f -b -p /opt/conda && \
    rm /tmp/Miniconda2-latest-Linux-x86_64.sh && \
# Install python packages
    /opt/conda/bin/conda install --yes \
    'ipykernel=4.3.1' 'jupyter_client=4.3.0' 'pandas=0.16*' 'matplotlib=1.4*' \
    'scipy=0.15*' 'seaborn=0.6*' 'scikit-learn=0.16*' && \
# Install r packages
    /opt/conda/bin/conda install --yes -c r r-essentials && \
    /opt/conda/bin/conda install --yes -c r r-base='3.3.1 1' && \
    /opt/conda/bin/conda install --yes -c r r-irkernel=0.7 && \
    /opt/conda/bin/conda clean --all --yes

# Flag to use old g++ ABI (necessary for compilation of r-igraph package)
RUN mkdir -p /root/.R && \
    echo "CXXFLAGS+=-D_GLIBCXX_USE_CXX11_ABI=0" >> /root/.R/Makevars

# Update boot script
COPY entrypoint.sh /entrypoint.sh
RUN chown root.root /entrypoint.sh
RUN chmod 700 /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
