#!/bin/bash

service ssh restart

if [ "$1" == "-d" ] ; then
  while [ 1 == 1 ] ; do
    sleep 1
  done
else
  exec "$@"
fi
