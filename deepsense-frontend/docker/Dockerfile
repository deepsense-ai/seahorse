FROM kyma/docker-nginx

ENV API_HOST http://localhost
ENV API_PORT 9080
ENV SOCKETS_ADDRESS http://localhost:15674/
ENV NOTEBOOKS_HOST http://localhost:8888

COPY build/ /var/www
COPY src/config.js.tmpl /tmp/
copy src/run.sh /tmp/

CMD '/tmp/run.sh'
