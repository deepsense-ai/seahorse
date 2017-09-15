# Copyright (c) 2016, CodiLime Inc.
FROM nginx:1.10

ENV API_HOST http://localhost
ENV API_PORT 9080
ENV SOCKETS_ADDRESS http://localhost:15674/
ENV NOTEBOOKS_HOST http://localhost:8888
ENV SESSION_POLLING_INTERVAL 5000

COPY dist/ /usr/share/nginx/html
COPY src/config.js.tmpl /tmp/
COPY src/run.sh /tmp/
# Update nginx configuration so it would request browser not to cache index.html
COPY default.conf /etc/nginx/conf.d/

ENTRYPOINT [ "/tmp/run.sh" ]
CMD ["nginx", "-g", "daemon off;"]
