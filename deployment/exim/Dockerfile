FROM alpine:3.4
RUN echo -en 'http://dl-cdn.alpinelinux.org/alpine/edge/main\n\
http://dl-cdn.alpinelinux.org/alpine/edge/testing\n' >> /etc/apk/repositories

RUN apk update && apk add exim tini
RUN mkdir -p /var/log/exim && \
touch /var/log/exim/mainlog /var/log/exim/paniclog /var/log/exim/rejectlog && \
chown exim:exim /var/log/exim/*log

COPY exim.conf /etc/exim/

ENTRYPOINT ["/sbin/tini"]

CMD ["/usr/sbin/exim", "-bd", "-q10m"]

EXPOSE 25
