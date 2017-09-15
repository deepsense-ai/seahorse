#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.

# Exit script after first erroneous instruction
set -e

export ETC_PROFILE_FILE="/etc/profile.d/deepsense_dev.sh"

export HADOOP_VERSION="2.6.0"
export HADOOP_DOWNLOAD_URL="http://ftp.ps.pl/pub/apache/hadoop/common/hadoop-2.6.0/hadoop-2.6.0.tar.gz"
export HADOOP_DOWNLOAD_URL_MIRROR="ftp://nas.intra.codilime.com/deepsense/development_environment/mirror/hadoop-2.6.0.tar.gz"

export USER="hadoop"
export USER_GROUP="hadoop"

useradd -m $USER -s /bin/bash

# Configure SSH
mkdir -p /home/$USER/.ssh
cp /tmp/ssh/id_rsa /home/$USER/.ssh/id_rsa
cp /tmp/ssh/id_rsa.pub /home/$USER/.ssh/id_rsa.pub
cp /tmp/ssh/id_rsa.pub /home/$USER/.ssh/authorized_keys

chown -R $USER:$USER_GROUP /home/$USER/.ssh
chmod 700 /home/$USER/.ssh
chmod 600 /home/$USER/.ssh/id_rsa
chmod 600 /home/$USER/.ssh/id_rsa.pub
chmod 600 /home/$USER/.ssh/authorized_keys

sed -i 's/Port 22/Port 8022/' /etc/ssh/sshd_config

# INSTALL HADOOP
wget $HADOOP_DOWNLOAD_URL_MIRROR || wget $HADOOP_DOWNLOAD_URL
tar -xzvf hadoop-$HADOOP_VERSION.tar.gz
rm hadoop-$HADOOP_VERSION.tar.gz
sudo mv hadoop-$HADOOP_VERSION /opt/hadoop

echo 'export HADOOP_SSH_OPTS="-p 8022"' >> /opt/hadoop/etc/hadoop/hadoop-env.sh

echo "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64" | sudo tee -a $ETC_PROFILE_FILE
echo "export CLASSPATH=:.:*.jar:lib:lib/*.jar" | sudo tee -a $ETC_PROFILE_FILE
echo "export PATH=\$PATH:\$JAVA_HOME/bin" | sudo tee -a $ETC_PROFILE_FILE

echo "export HADOOP_CONF=/opt/hadoop/etc/hadoop" | sudo tee -a $ETC_PROFILE_FILE
echo "export HADOOP_PREFIX=/opt/hadoop" | sudo tee -a $ETC_PROFILE_FILE
echo "export PATH=\$PATH:\$HADOOP_PREFIX/bin" | sudo tee -a $ETC_PROFILE_FILE

# Copy configuration
cp /tmp/hadoop/* /opt/hadoop/etc/hadoop
chown -R $USER:$USER_GROUP /opt/hadoop
