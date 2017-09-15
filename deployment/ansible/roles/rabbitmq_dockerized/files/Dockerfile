FROM rabbitmq:latest

COPY rabbitmq.config /etc/rabbitmq/
COPY definitions.json /etc/rabbitmq
COPY enabled_plugins /etc/rabbitmq

EXPOSE 61613
EXPOSE 15672
EXPOSE 15674
