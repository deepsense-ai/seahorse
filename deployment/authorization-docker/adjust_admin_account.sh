#!/bin/bash -ex
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


/opt/replace_envs.sh /opt/create_admin_user.sql
/opt/replace_envs.sh /opt/update_admin_user.sql

run_sql_script () {
  java -cp /opt/h2*.jar org.h2.tools.RunScript \
    -user "" \
    -url "${JDBC_URL}" \
    -script $1
}
# Wait until database is created.
until run_sql_script /opt/check_if_database_ready.sql &> /dev/null
do
  echo "Waiting for database to initialize..."
  sleep 1
done
# Create admin user, or, if already exists, update admin's username and email.
run_sql_script /opt/create_admin_user.sql || run_sql_script /opt/update_admin_user.sql
