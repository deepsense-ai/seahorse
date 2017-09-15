# Copyright (c) 2016, CodiLime Inc.
FROM hortonworks/cloudbreak-uaa:3.6.0

ENV H2_VERSION 1.4.192

# build-essential gcc ruby-dev rubygems libssl-dev packages are needed by cf-uaac
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y ruby ruby-dev rubygems mailutils build-essential openssl zip && \
    gem install cf-uaac && \
    apt-get clean && \
    apt-get autoremove && \
    rm -rf /var/lib/apt/lists/*

# Exim4 should be able to send mails to whole internet
ADD update-exim4.conf.conf /etc/exim4/
ADD messages.properties /opt/WEB-INF/classes/
ADD uaa.yml /opt/uaa/

# Add h2 driver to cloudfoundry identity uaa
# Add messages.properties with new information for activation email
RUN mkdir -p /opt/WEB-INF/lib/ && \
    wget http://repo2.maven.org/maven2/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar \
        -O /opt/WEB-INF/lib/h2-${H2_VERSION}.jar && \
    cd /opt/ && zip -r /tomcat/webapps/ROOT.war WEB-INF/ && \
    rm -Rf /opt/WEB-INF && \
    mv /tomcat/webapps/ROOT.war /tomcat/webapps/authorization.war && \
    rm -Rf /uaa/uaa.yml

# New run.sh starts postfix MTA
ADD docker-dummy-authorization.sh /opt/
ADD run.sh /tmp/
RUN chmod +x /tmp/run.sh

