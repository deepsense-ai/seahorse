#!/bin/bash
#
# Copyright (c) 2016, CodiLime Inc.
# NOTE: based on https://github.com/mesoscloud/mesos-master
#

PRINCIPAL=${PRINCIPAL:-root}

if [ -n "$SECRET" ]; then
    export MESOS_AUTHENTICATE=true
    export MESOS_AUTHENTICATE_SLAVES=true
    touch /tmp/credentials
    chmod 600 /tmp/credentials
    printf '%s %s' "$PRINCIPAL" "$SECRET" > /tmp/credentials
    export MESOS_CREDENTIALS=/tmp/credentials
fi

exec "$@"
