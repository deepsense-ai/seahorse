# Copyright (c) 2016, CodiLime Inc.
FROM quay.io/deepsense_io/deepsense-spark-base:1.6.1

# Install krb5
RUN apt-get update && \
    apt-get install -y krb5-user unzip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install ktinit
RUN cd / && \
    wget http://krb5jwt.s3-website-us-east-1.amazonaws.com/kerberos-jwt-1.1.zip && \
    unzip -o kerberos-jwt-1.1.zip && \
    rm kerberos-jwt-1.1.zip
