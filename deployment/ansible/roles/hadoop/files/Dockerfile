# Copyright (c) 2015, CodiLime Inc.

FROM ubuntu:14.04

RUN apt-get update && apt-get install openjdk-7-jdk wget openssh-server -y
RUN mkdir -p /tmp/hadoop

# Rebuild and istall libpam with --disable-audit option
RUN apt-get -y build-dep pam
RUN export CONFIGURE_OPTS=--disable-audit && cd /root && apt-get -b source pam && dpkg -i libpam-doc*.deb libpam-modules*.deb libpam-runtime*.deb libpam0g*.deb

COPY scripts/install.sh /tmp/
COPY scripts/init.sh /tmp/
COPY scripts/run.sh /tmp/

COPY conf/hadoop/core-site.xml.template /tmp/hadoop/
COPY conf/hadoop/hdfs-site.xml.template /tmp/hadoop/
COPY conf/hadoop/yarn-site.xml.template /tmp/hadoop/

COPY conf/ssh/id_rsa /tmp/ssh/
COPY conf/ssh/id_rsa.pub /tmp/ssh/

RUN /tmp/install.sh

EXPOSE 50010 50020 50070 50075 50090
EXPOSE 8020 19888
EXPOSE 8030 8031 8032 8033 8040 8042 8088
EXPOSE 49707 8022

ENTRYPOINT ["/tmp/init.sh"]

CMD ["-d"]
