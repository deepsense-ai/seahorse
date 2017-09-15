# Copyright (c) 2016, CodiLime Inc.
FROM nginx:1.10

ENV API_HOST http://localhost
ENV API_PORT 9080
ENV SOCKETS_ADDRESS http://localhost:15674/
ENV NOTEBOOKS_HOST http://localhost:8888
ENV SESSION_POLLING_INTERVAL 5000

COPY build/ /usr/share/nginx/html
COPY src/config.js.tmpl /tmp/
copy src/run.sh /tmp/

ENTRYPOINT [ "/tmp/run.sh" ]
CMD ["nginx", "-g", "daemon off;"]
