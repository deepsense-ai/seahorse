#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.

# This script is run inside of Vagrant image. Do not run it manually!

set -e

# Update repositories
apt-get -y update

# Install prerequisites
apt-get -y install apt-transport-https ca-certificates
#apt-get -y install linux-image-extra-$(uname -r)
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

# Install docker-compose
pip install docker-compose
