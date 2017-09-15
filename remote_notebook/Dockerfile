FROM jupyter/minimal-notebook:2d878db5cbff

USER root

RUN wget -q https://bootstrap.pypa.io/get-pip.py && python2.7 get-pip.py

RUN pip2 install pika ipykernel
RUN pip install pika ipykernel

COPY code/forwarding_kernel/ /usr/local/share/jupyter/kernels/pyspark/
COPY code/rabbit_mq_client.py code/socket_forwarder.py code/utils.py code/notebook_server_client.py /usr/local/share/jupyter/kernels/pyspark/

COPY jupyter_notebook_config.py /home/jovyan/.jupyter
COPY wmcontents /opt/conda/lib/python3.5/site-packages/wmcontents

EXPOSE 8888

ENV HEARTBEAT_INTERVAL 2.0
ENV MISSED_HEARTBEAT_LIMIT 30

ENTRYPOINT ["tini", "--"]
CMD ["start-notebook.sh"]
