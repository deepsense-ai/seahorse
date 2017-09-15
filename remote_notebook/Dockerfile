FROM jupyter/minimal-notebook:6939ea1b1df3

USER root

RUN wget -q https://bootstrap.pypa.io/get-pip.py && python2.7 get-pip.py

RUN pip2 install pika ipykernel
RUN pip install pika ipykernel

COPY code/forwarding_kernel/ /usr/local/share/jupyter/kernels/pyspark/
COPY code/rabbit_mq_client.py \
     code/socket_forwarder.py \
     code/utils.py \
     code/notebook_server_client.py \
     /usr/local/share/jupyter/kernels/pyspark/
COPY code/forwarding_kernel_py/ /usr/local/share/jupyter/kernels/forwarding_kernel_py/
COPY code/forwarding_kernel_r/ /usr/local/share/jupyter/kernels/forwarding_kernel_r/
# seahorse_notebook_path is copied two times, because it is needed
# by forwarding_kernel (python 2.7) and by notebook server (python 3.5)
COPY seahorse_notebook_path /usr/local/lib/python2.7/site-packages

COPY jupyter_notebook_config.py /home/jovyan/.jupyter
COPY wmcontents /opt/conda/lib/python3.5/site-packages/wmcontents

# seahorse_notebook_path is copied two times, because it is needed
# by forwarding_kernel (python 2.7) and by notebook server (python 3.5)
COPY seahorse_notebook_path /opt/conda/lib/python3.5/site-packages/
COPY execute_saver /opt/conda/lib/python3.5/site-packages/execute_saver
COPY headless_notebook_handler.py /opt/conda/lib/python3.5/site-packages/headless_notebook_handler/

EXPOSE 8888

ENV MQ_USER guest
ENV MQ_PASS guest
ENV HEARTBEAT_INTERVAL 2.0
ENV MISSED_HEARTBEAT_LIMIT 30

ENTRYPOINT ["tini", "--"]
CMD ["start-notebook.sh"]
