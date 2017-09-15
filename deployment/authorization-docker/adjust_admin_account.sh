#!/bin/bash -ex

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
