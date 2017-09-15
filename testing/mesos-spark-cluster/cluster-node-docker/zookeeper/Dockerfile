# Copyright (c) 2016, CodiLime Inc.
# NOTE: based on https://github.com/mesoscloud/zookeeper

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

# https://www.apache.org/mirrors/dist.html
RUN curl -fL http://archive.apache.org/dist/zookeeper/zookeeper-3.4.9/zookeeper-3.4.9.tar.gz | tar xzf - -C /opt && \
mv /opt/zookeeper-3.4.9 /opt/zookeeper

VOLUME /tmp/zookeeper

COPY entrypoint.sh /

ENTRYPOINT ["/usr/local/bin/dumb-init", "/entrypoint.sh"]

ENV PATH /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/zookeeper/bin

CMD ["zkServer.sh", "start-foreground"]
