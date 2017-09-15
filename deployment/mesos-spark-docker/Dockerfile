# Copyright (c) 2016, CodiLime Inc.

FROM quay.io/deepsense_io/deepsense-spark:1.6.1

# Install Mesos dependencies
RUN apt-get update && apt-get install -y \
  build-essential \
  python-dev \
  libcurl4-nss-dev \
  libsasl2-dev \
  libsasl2-modules \
  maven \
  libapr1-dev \
  libsvn-dev

RUN apt-get update && apt-get install -y zlib1g-dev
RUN apt-get update && apt-get install -y openjdk-8-jdk

# Compile and install Mesos (compilation phase uses 6 threads for speed up this process)
ENV MESOS_VERSION 1.0.0
ENV MESOS_ARTIFACT_FILENAME mesos-${MESOS_VERSION}.tar.gz
RUN wget http://archive.apache.org/dist/mesos/${MESOS_VERSION}/${MESOS_ARTIFACT_FILENAME} \
  && tar -xf ${MESOS_ARTIFACT_FILENAME} \
  && cd mesos-${MESOS_VERSION} \
  && mkdir build \
  && cd build \
  && ../configure \
  && make -j 6 \
  && make install

RUN ln -s /usr/local/lib/libmesos-1.0.0.so /usr/lib/libmesos.so
