# Copyright (c) 2016, CodiLime Inc.
# NOTE: based on https://github.com/mesoscloud/mesos-master and https://github.com/mesoscloud/mesos-slave

FROM ubuntu:14.04
MAINTAINER DeepSense.io

ARG SPARK_VERSION
ARG HADOOP_VERSION

# To suppress some warnings
ENV TERM=xterm

# Install Java
RUN apt-get update && apt-get install -y --no-install-recommends software-properties-common python-software-properties ca-certificates curl
RUN add-apt-repository ppa:webupd8team/java -y
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
RUN apt-get update && apt-get install -y oracle-java8-installer oracle-java8-set-default
RUN rm -rf /var/lib/apt/lists/*

# https://github.com/Yelp/dumb-init
RUN curl -fLsS -o /usr/local/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.0.2/dumb-init_1.0.2_amd64 && chmod +x /usr/local/bin/dumb-init

# Install Mesos
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF && \
echo deb http://repos.mesosphere.io/ubuntu trusty main > /etc/apt/sources.list.d/mesosphere.list && \
apt-get update && \
apt-get install --no-install-recommends -y mesos=1.0.1-2.0.93.ubuntu1404 && \
rm -rf /var/lib/apt/lists/*

ENV MESOS_WORK_DIR /tmp/mesos

# Download spark executor
RUN mkdir /spark-$SPARK_VERSION && \
    wget -q -O /spark-$SPARK_VERSION/packed.tgz \
    http://d3kbcqa49mib13.cloudfront.net/spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz && \
    tar -xzf /spark-$SPARK_VERSION/packed.tgz -C /spark-$SPARK_VERSION && \
    rm /spark-$SPARK_VERSION/packed.tgz

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

ENV PATH $PATH:/opt/conda/bin

VOLUME /tmp/mesos

COPY entrypoint-master.sh /

ENTRYPOINT ["/usr/local/bin/dumb-init", "/entrypoint-master.sh"]
CMD ["/usr/sbin/mesos-master"]
