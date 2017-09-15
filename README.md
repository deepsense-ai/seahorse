Running the proxy app (dev mode)
================================

1. Install dependencies

    $ npm install

2. Make sure all ENV variables are set (check next paragraphs)

3. Run the application

    $ npm start

Building docker image
=====================

    $ docker build -t deepsense-proxy .

Needed env variables
====================

(Modify addresses for specific environment)
```
export VCAP_SERVICES='{"user-provided":[{"credentials":{"authorizationUri":"http://login.seahorse-krb.gotapaas.eu/oauth/authorize","logoutUri":"http://login.seahorse-krb.gotapaas.eu/logout.do","tokenUri":"http://uaa.seahorse-krb.gotapaas.eu/oauth/token","clientId":"seahorse_proxy_local","clientSecret":"seahorse01","userInfoUri":"http://login.seahorse-krb.gotapaas.eu/userinfo"},"name":"sso"},{"credentials":{"host":"http://workflowmanager:9080"},"name":"workflow-manager"},{"credentials":{"host":"http://sessionmanager:9082"},"name":"session-manager"},{"credentials":{"host":"http://notebooks:8888"},"name":"jupyter"},{"credentials":{"host":"http://rabbitmq:15674"},"name":"rabbitmq"},{"credentials":{"host":"http://frontend:80"},"name":"frontend"}]}'
export DOMAIN="seahorse-krb.gotapaas.eu"
export ORGANIZATION_ID="e06d477a-c8bb-48f0-baeb-3d709578d8af"
export CHECK_ORGANIZATION="true"
```

# Copyright (c) 2016, CodiLime Inc.
