FROM rabbitmq:latest

COPY rabbitmq.config /etc/rabbitmq/

RUN rabbitmq-plugins enable rabbitmq_stomp
RUN rabbitmq-plugins enable rabbitmq_web_stomp
RUN rabbitmq-plugins enable rabbitmq_management

EXPOSE 61613
EXPOSE 15672
EXPOSE 15674
