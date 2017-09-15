# Copyright (c) 2016, CodiLime Inc.
# NOTE: based on https://github.com/mesoscloud/mesos-master

FROM ubuntu:14.04
MAINTAINER DeepSense.io

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

CMD ["/usr/sbin/mesos-master"]

ENV MESOS_WORK_DIR /tmp/mesos

VOLUME /tmp/mesos

COPY entrypoint.sh /

ENTRYPOINT ["/usr/local/bin/dumb-init", "/entrypoint.sh"]
