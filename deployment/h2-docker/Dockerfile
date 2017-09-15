# Copyright (c) 2016, CodiLime Inc.
FROM anapsix/alpine-java:jre8

ENV H2_VERSION 1.4.192
ENV DATA_DIR /opt/h2-data

RUN wget http://repo2.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar \
        -O /opt/h2-${H2_VERSION}.jar && \
    mkdir -p ${DATA_DIR}

EXPOSE 1521

CMD java -cp /opt/h2-${H2_VERSION}.jar org.h2.tools.Server \
        -tcp -tcpAllowOthers -tcpPort 1521 \
        -baseDir ${DATA_DIR}
