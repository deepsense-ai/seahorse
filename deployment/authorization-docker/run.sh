#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

service exim4 start
export CLOUD_FOUNDRY_CONFIG_PATH="/opt/uaa"

export JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
if [[ -n "$ENABLE_AUTHORIZATION" && "$ENABLE_AUTHORIZATION" == "true" ]]; then
  /opt/adjust_admin_account.sh &
  $CATALINA_HOME/bin/catalina.sh run
else
  echo "Authorization is not starting because ENABLE_AUTHORIZATION is not true"
  /opt/docker-dummy-authorization.sh
fi
