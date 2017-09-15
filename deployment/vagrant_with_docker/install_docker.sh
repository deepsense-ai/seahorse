#!/bin/bash

# Copyright 2016 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# This script is run inside of Vagrant image. Do not run it manually!

set -e

# Update repositories
apt-get -y update

# Install prerequisites
apt-get -y install apt-transport-https ca-certificates

# Install 3.19 kernel so Notebooks will work
apt-get -y install linux-generic-lts-vivid

apt-get -y install python-pip

# Add docker repository
apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" > /etc/apt/sources.list.d/docker.list

# Update repositories
apt-get -y update

# Install docker
apt-get -y install docker-engine

# Add vagrant user to docker group
usermod -aG docker vagrant

# Start docker daemon
service docker restart

# Disable docker daemon start on system startup
echo 'manual' > /etc/init/docker.override

# Install docker-compose
pip install docker-compose

apt-get clean
rm -rf /var/lib/apt/lists/*
