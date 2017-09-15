# Copyright (c) 2015, CodiLime Inc.

# Example script for creating Jupyter demo container
# with configured WorkflowManager notebook storage.

FROM jupyter/minimal-notebook
MAINTAINER Jan Burka <jan.burka@codilime.com>

USER jovyan

# Install Python 3 packages
RUN conda install --yes \
    'ipywidgets=4.0*' \
    'pandas=0.16*' \
    'matplotlib=1.4*' \
    'scipy=0.15*' \
    'seaborn=0.6*' \
    'scikit-learn=0.16*'

# Install Python 2 packages and kernel spec
RUN conda create -p $CONDA_DIR/envs/python2 -c https://conda.anaconda.org/prometeia python=2.7 \
    'ipython=4.0*' \
    'ipywidgets=4.0*' \
    'pandas=0.16*' \
    'matplotlib=1.4*' \
    'scipy=0.15*' \
    'seaborn=0.6*' \
    'scikit-learn=0.16*' \
    'pika=0.10*' \
    pyzmq

USER root

# Install Java
RUN apt-get update && apt-get install openjdk-7-jdk -y
ENV JAVA_HOME /usr/bin/jvm/java-7-openjdk-amd64

# Install Spark 1.5.1 for Hadoop 2.6
RUN apt-get install curl -y
RUN curl -s http://d3kbcqa49mib13.cloudfront.net/spark-1.5.1-bin-hadoop2.6.tgz | tar -xz -C /opt
RUN cd /opt && ln -s spark-1.5.1-bin-hadoop2.6 spark
ENV SPARK_HOME /opt/spark

# Install Python 2 kernel spec globally to avoid permission problems when NB_UID
# switching at runtime.
RUN $CONDA_DIR/envs/python2/bin/python \
    $CONDA_DIR/envs/python2/bin/ipython \
    kernelspec install-self

# Install pykernel
RUN pip install ipykernel

# Install custom kernel
COPY kernels/pyspark /usr/local/share/jupyter/kernels/pyspark

# Install wmcontents python module
COPY wmcontents /opt/conda/lib/python3.4/site-packages/wmcontents

# Set WorkflowManager as notebook storage
COPY jupyter_notebook_wm_config.py /home/jovyan/.jupyter/
RUN cat /home/jovyan/.jupyter/jupyter_notebook_wm_config.py >> /home/jovyan/.jupyter/jupyter_notebook_config.py

# Set up start script
COPY start-notebook-server.sh /home/$NB_USER/work/
RUN chmod +x /home/$NB_USER/work/start-notebook-server.sh

EXPOSE 8888

ENTRYPOINT ["tini", "--"]
CMD ["./start-notebook-server.sh"]