# Copyright (c) 2016, CodiLime Inc.
FROM docker-repo.deepsense.codilime.com/intel/tap-base-java:java8-jessie

RUN apt-get update && \
    apt-get install --no-install-recommends --no-install-suggests -y krb5-user wget unzip tar && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir /var/krb5kdc && \
    chmod a+w /etc/krb5.conf

ENV SPARK_HOME="/opt/spark"
RUN mkdir $SPARK_HOME \
        && SPARK_TAR="spark-1.6.1-bin-hadoop2.6.tgz" \
        && wget http://www-us.apache.org/dist/spark/spark-1.6.1/$SPARK_TAR \
        && tar -zxf $SPARK_TAR -C $SPARK_HOME --strip-components=1 && rm -f $SPARK_TAR \
        && cp $SPARK_HOME/conf/log4j.properties.template $SPARK_HOME/conf/log4j.properties
