# Copyright (c) 2016, CodiLime Inc.

FROM seahorse-spark:${BASE_IMAGE_TAG}

ENV MESOS_VERSION 1.0.0
ENV MESOS_ARTIFACT_FILENAME mesos-${MESOS_VERSION}.tar.gz

# Install Mesos dependencies
# Compile and install Mesos (compilation phase uses 6 threads for speed up this process)
# Uninstall Mesos build dependencies
RUN apt-get update && apt-get install -y \
    openjdk-8-jdk \
    python-dev \
    libcurl4-nss-dev \
    libsasl2-dev \
    libsasl2-modules \
    maven \
    libapr1-dev \
    libsvn-dev \
    zlib1g-dev \
  && wget http://archive.apache.org/dist/mesos/${MESOS_VERSION}/${MESOS_ARTIFACT_FILENAME} \
  && tar -xf ${MESOS_ARTIFACT_FILENAME} \
  && cd mesos-${MESOS_VERSION} \
  && mkdir build \
  && cd build \
  && ../configure \
  && make -j 6 \
  && cp src/.libs/libmesos-${MESOS_VERSION}.so /usr/local/lib/libmesos-${MESOS_VERSION}.so \
  && cd ../.. \
  && rm -rf mesos-${MESOS_VERSION} ${MESOS_ARTIFACT_FILENAME} \
  && apt-get purge -y \
    openjdk-8-jdk \
    python-dev \
    libsasl2-dev \
    libsasl2-modules \
    maven \
    zlib1g-dev \
  && apt-get clean \
  && apt-get autoremove -y \
  && rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/local/lib/libmesos-${MESOS_VERSION}.so /usr/lib/libmesos.so
