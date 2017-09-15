# Copyright (c) 2016, CodiLime Inc.
FROM ubuntu:16.04

# Install required packages
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates && \
    echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823 && \
    apt-get update && \
    apt-get install -y nano sudo curl bzip2 wget git openjdk-8-jdk sbt && \
    curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash - && \
    sudo apt-get install -y nodejs

RUN mkdir /cache && \
    mkdir /cache/.sbt /cache/.ivy2 /cache/.m2 && \
    ln -s /cache/.sbt /root/.sbt && \
    ln -s /cache/.ivy2 /root/.ivy2 && \
    ln -s /cache/.m2 /root/.m2

ADD build.sh /tmp/build.sh
RUN chmod +x /tmp/build.sh

CMD [ "/tmp/build.sh" ]
