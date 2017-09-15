# Copyright (c) 2016, CodiLime Inc.
FROM centos:6.6

RUN yum --assumeyes install openssl-devel java-1.8.0-openjdk-headless.x86_64 krb5-workstation wget unzip tar \
        && wget http://krb5jwt.s3-website-us-east-1.amazonaws.com/kerberos-jwt-1.1.zip \
        && unzip -o kerberos-jwt-1.1.zip \
        && mkdir /var/krb5kdc \
        && chmod a+w /etc/krb5.conf

ENV SPARK_HOME="/opt/spark"
RUN mkdir $SPARK_HOME \
        && SPARK_TAR="spark-1.6.1-bin-hadoop2.6.tgz" \
        && wget http://www-us.apache.org/dist/spark/spark-1.6.1/$SPARK_TAR \
        && tar -zxf $SPARK_TAR -C $SPARK_HOME --strip-components=1 && rm -f $SPARK_TAR \
        && cp $SPARK_HOME/conf/log4j.properties.template $SPARK_HOME/conf/log4j.properties
