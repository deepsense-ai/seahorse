#!/bin/bash
#
# Copyright (c) 2016, CodiLime Inc.
# NOTE: based on https://github.com/mesoscloud/mesos-slave
#

PRINCIPAL=${PRINCIPAL:-root}

if [ -n "$SECRET" ]; then
    touch /tmp/credential
    chmod 600 /tmp/credential
    printf '%s %s' "$PRINCIPAL" "$SECRET" > /tmp/credential
    export MESOS_CREDENTIAL=/tmp/credential
fi

exec "$@"
