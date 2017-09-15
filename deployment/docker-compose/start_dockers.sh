#!/bin/bash
LOCAL_MQ_PORT=5672
CDH_MQ_PORT=$(( ( RANDOM % 1000 )  + 20000 ))
MASTER0_MQ_PORT=$(( ( RANDOM % 1000 )  + 21000 ))

ssh -t -t -R 0.0.0.0:$CDH_MQ_PORT:localhost:$LOCAL_MQ_PORT eu-seahorse-cdh "ssh -t -t -R 0.0.0.0:$MASTER0_MQ_PORT:localhost:$CDH_MQ_PORT cdh-master-0" &
MQ_TUNNEL_PID=$!

LOCAL_WM_PORT=9080
CDH_WM_PORT=$(( ( RANDOM % 1000 )  + 22000 ))
MASTER0_WM_PORT=$(( ( RANDOM % 1000 )  + 23000 ))

ssh -t -t -R 0.0.0.0:$CDH_WM_PORT:localhost:$LOCAL_WM_PORT eu-seahorse-cdh "ssh -t -t -R 0.0.0.0:$MASTER0_WM_PORT:localhost:$CDH_WM_PORT cdh-master-0" &
WM_TUNNEL_PID=$!

echo "MQ_PORT=$MASTER0_MQ_PORT, WM_PORT=$MASTER0_WM_PORT"

python fill_docker_compose_template.py $MASTER0_MQ_PORT $MASTER0_WM_PORT > docker-compose.yml
docker-compose up

echo "KILLING TUNNELS!"
kill -s 9 $MQ_TUNNEL_PID
kill -s 9 $WM_TUNNEL_PID
