#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.

# Set kerberos config
[ -z "$KRB5_CONFIG" ] || cp $KRB5_CONFIG /etc/krb5.conf

# Create temp dir for user ccache files
mkdir /tmp/krb5

exec "$@"
