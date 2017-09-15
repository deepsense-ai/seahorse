#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

service exim4 start
[ -n "$UAA_CONFIG_URL" ] && curl -Lo /uaa/uaa.yml $UAA_CONFIG_URL

# if uaa.yml doesn't exist in /uaa/uaa.yml Seahorse uses default configuration in /opt/uaa
[ ! -f "$UAA_CONFIG_PATH/uaa.yml" ] && export CLOUD_FOUNDRY_CONFIG_PATH="/opt/uaa"

export JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
if [[ -n "$ENABLE_AUTHORIZATION" && "$ENABLE_AUTHORIZATION" == "true" ]]; then
  $CATALINA_HOME/bin/catalina.sh run
else
  echo "Authorization is not starting because ENABLE_AUTHORIZATION is not true"
  /opt/docker-dummy-authorization.sh
fi
