#!/bin/bash -ex
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
