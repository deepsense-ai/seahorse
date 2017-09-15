Running the proxy app (dev mode)
================================

1. Install dependencies

    $ npm install

2. Set VCAP_SERVICES env variable (see below)

3. Run the application

    $ npm start



Running the proxy docker container
==================================

1. Build docker image

    $ docker build -t deepsense-proxy .

2. Set VCAP_SERVICES env variable (see below)

3. Run docker image

    $ docker run -d -e VCAP_SERVICES="$VCAP_SERVICES" -p 8080:8080 deepsense-proxy



Setting VCAP_SERVICES variable
==============================

(Modify addresses for specific environment)

$ export VCAP_SERVICES='{
  "user-provided": [
    {
      "credentials": {
        "authorizationUri": "http://login.seahorse.gotapaas.eu/oauth/authorize",
        "logoutUri": "http://login.seahorse.gotapaas.eu/logout.do",
        "tokenUri": "http://uaa.seahorse.gotapaas.eu/oauth/token",
        "clientId": "<CLIENT-ID>",
        "clientSecret": "<CLIENT-SECRET>",
        "userInfoUri": "http://login.seahorse.gotapaas.eu/userinfo"
      },
      "name": "sso"
    },
    {
      "credentials": {
        "host": "http://172.28.128.100:9080"
      },
      "name": "workflow-manager"
    },
    {
      "credentials": {
        "host": "http://172.28.128.100:9082"
      },
      "name": "session-manager"
    },
    {
      "credentials": {
        "host": "http://172.28.128.100:8888"
      },
      "name": "jupyter"
    },
    {
      "credentials": {
        "host": "http://172.28.128.100:15674"
      },
      "name": "rabbitmq"
    },
    {
      "credentials": {
        "host": "http://172.28.128.100:8000"
      },
      "name": "frontend"
    }
  ]
}'

# Copyright (c) 2016, CodiLime Inc.
