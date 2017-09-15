#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.

# Exit script after first erroneous instruction
set -e

export ETC_PROFILE_FILE="/etc/profile.d/deepsense_dev.sh"

# Create user
export USER=spark
export USER_GROUP=spark

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

sed -i 's/Port 22/Port 9022/' /etc/ssh/sshd_config

# INSTALL SCALA
export SCALA_VERSION="2.11.6"
export SCALA_DOWNLOAD_URL="http://downloads.typesafe.com/scala/2.11.6/scala-2.11.6.tgz"
export SCALA_DOWNLOAD_URL_MIRROR="ftp://nas.intra.codilime.com/deepsense/development_environment/mirror/scala-2.11.6.tgz"

wget $SCALA_DOWNLOAD_URL_MIRROR || wget $SCALA_DOWNLOAD_URL
tar xvfz scala-$SCALA_VERSION.tgz
rm scala-$SCALA_VERSION.tgz
sudo mv scala-$SCALA_VERSION /opt/scala/
chown -R $USER:$USER_GROUP /opt/scala

echo "export SCALA_HOME=/opt/scala" | sudo tee -a $ETC_PROFILE_FILE
echo "export PATH=\$PATH:\$SCALA_HOME/bin" | sudo tee -a $ETC_PROFILE_FILE

# INSTALL SPARK FROM COMPILED PACKAGE ({{spark_package_filename}})
export SPARK_VERSION="{{spark_version}}"
sudo tar -zxvf /tmp/{{spark_package_filename}}
rm /tmp/{{spark_package_filename}}
sudo mv spark-$SPARK_VERSION /opt/spark

# Prepare SPARK environment variables
cp /opt/spark/conf/spark-env.sh.template /opt/spark/conf/spark-env.sh
cat >> /opt/spark/conf/spark-env.sh << EOF

export SPARK_SCALA_VERSION=2.11
export SPARK_SSH_OPTS="-p 9022"
EOF

sudo chown -R $USER:$USER_GROUP /opt/spark
