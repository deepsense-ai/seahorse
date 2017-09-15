#!/bin/bash

set -e

: ${RABBITMQ_USER:=guest}
: ${RABBITMQ_PASS:=guest}

SALT=$(head -c 4 /dev/urandom)

# Generate hashed password as described in http://lists.rabbitmq.com/pipermail/rabbitmq-discuss/2011-May/012765.html
# HASH = BASE64 ( SALT || SHA256 ( SALT || PASS ) )
HASH=$(echo -ne ${SALT}$(echo -n "${SALT}${RABBITMQ_PASS}" | sha256sum | cut -d' ' -f1 | sed 's/\(..\)/\\x\1/g') | base64)

sed -e "s#USERNAME#${RABBITMQ_USER}#" -e "s#PASSWORD#${HASH}#" /tmp/definitions.json.tmpl > /etc/rabbitmq/definitions.json

exec "$@"
