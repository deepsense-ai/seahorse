#!/bin/bash

CREATION_DATE_FILE=/trial/.seahorse_creation_time

cat $CREATION_DATE_FILE

if [ ! -f $CREATION_DATE_FILE ]; then
    echo "Proxy initialization"
    mkdir -p /trial
    date +%Y-%m-%d > $CREATION_DATE_FILE
fi

export SEAHORSE_CREATION_DATE=`cat $CREATION_DATE_FILE`

npm start
