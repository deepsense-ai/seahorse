#!/bin/bash
# Copyright 2017 deepsense.ai (CodiLime, Inc)
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


set -e

: ${RABBITMQ_USER:=guest}
: ${RABBITMQ_PASS:=guest}

SALT="ABCD"

# Generate hashed password as described in http://lists.rabbitmq.com/pipermail/rabbitmq-discuss/2011-May/012765.html
# HASH = BASE64 ( SALT || SHA256 ( SALT || PASS ) )
HASH=$(echo -ne ${SALT}$(echo -n "${SALT}${RABBITMQ_PASS}" | sha256sum | cut -d' ' -f1 | sed 's/\(..\)/\\x\1/g') | base64)

sed -e "s#USERNAME#${RABBITMQ_USER}#" -e "s#PASSWORD#${HASH}#" /tmp/definitions.json.tmpl > /etc/rabbitmq/definitions.json

exec "$@"
