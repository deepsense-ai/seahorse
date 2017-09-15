#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.

[ "${ENV_PREFIX}" != "" ] || exit 1

# Modify /etc/hosts
grep -v ${ENV_PREFIX} /etc/hosts > hosts.new
grep ${ENV_PREFIX} /tmp/hosts.orig >> hosts.new
cat hosts.new > /etc/hosts
rm hosts.new

# Start SSH daemon
service ssh restart

# Start services
sudo -E su hadoop --preserve-environment -c "/tmp/run.sh $@"
