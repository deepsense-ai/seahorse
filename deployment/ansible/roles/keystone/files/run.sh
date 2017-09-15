#!/bin/bash -x

# Copyright (c) 2015, CodiLime, Inc.

# Script installs and configures keystone for DS.io development environment

# Create empty database if not present
if [ ! -f /var/lib/keystone/keystone.db ]
then
  cp /tmp/keystone.db.empty /var/lib/keystone/keystone.db
fi

# Set permissions
chown -R keystone:keystone /var/lib/keystone

PIDFILE=/root/keystone.pid

start-stop-daemon --start --chuid keystone \
            --make-pidfile \
            --pidfile $PIDFILE \
            --name keystone \
            --exec /usr/bin/keystone-all &

# Wait until Keystone started
sleep 20

export OS_SERVICE_ENDPOINT="http://127.0.0.1:35357/v2.0"
export OS_SERVICE_TOKEN="ADMIN"

ROLES_COUNT=`keystone role-list | wc -l`

if [ "$ROLES_COUNT" == 5 ]; then
  # List of DS.io specific roles
  ROLES=("workflows:get
  workflows:update
  workflows:create
  workflows:list
  workflows:delete
  workflows:launch
  workflows:abort
  entities:get
  entities:create
  entities:update
  entities:delete")

  # Add an "admin" role. Keystone interprets "admin" role in a special way
  keystone role-create --name admin
  for role in $ROLES
  do
    keystone role-create --name $role
  done

  # Create a special tenant "service" for DS.io service
  keystone tenant-create --name service

  # Create special users for entity storage and workflow manager service
  keystone user-create --name entity-storage --tenant service --pass $ES_PASSWORD
  keystone user-role-add --user entity-storage --role admin --tenant service
  keystone user-create --name workflow-manager --tenant service --pass $WM_PASSWORD
  keystone user-role-add --user workflow-manager --role admin --tenant service

  # Create tenants and users for integration test and demo purposes
  keystone tenant-create --name integration-limited
  keystone user-create --name integration-limited --tenant integration-limited \
    --pass daidohquiet6ouF3

  TENANTS=("integration-full" "demo0" "demo1")
  USERS=("integration-full" "demo0" "demo1")
  PASSWORDS=("zi2ieMee8aeM4thi" "demo0pass" "demo1pass")

  for ix in ${!TENANTS[*]}
  do
    tenant="${TENANTS[$ix]}"
    user="${USERS[$ix]}"
    password="${PASSWORDS[$ix]}"
    keystone tenant-create --name $tenant
    keystone user-create --name $user --tenant $tenant --pass $password
    for role in $ROLES
    do
      keystone user-role-add --user $user --role $role --tenant $tenant
    done
  done

  unset OS_SERVICE_ENDPOINT

else
  echo "Roles already created - skipping!"
fi

# Restart Keystone. Begin with shutdown...
kill `cat $PIDFILE`
sleep 5

sed "s/#admin_token=ADMIN/admin_token=$ADMIN_TOKEN/g" -i /etc/keystone/keystone.conf
sed "s/#expiration=3600/expiration=31536000/g" -i /etc/keystone/keystone.conf

start-stop-daemon --start --chuid keystone \
            --make-pidfile \
            --pidfile $PIDFILE \
            --name keystone \
            --exec /usr/bin/keystone-all
