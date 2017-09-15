# Copyright (c) 2015, CodiLime Inc.

# Example script for creating Jupyter demo container
# with configured WorkflowManager notebook storage.

FROM jupyter/demo
MAINTAINER Jan Burka <jan.burka@codilime.com>

# Install wmcontents python module
COPY wmcontents /opt/conda/lib/python3.4/site-packages/wmcontents

USER jovyan

# Set WorkflowManager as notebook storage
COPY jupyter_notebook_wm_config.py /home/jovyan/.jupyter/
RUN cat /home/jovyan/.jupyter/jupyter_notebook_wm_config.py >> /home/jovyan/.jupyter/jupyter_notebook_config.py

USER root

EXPOSE 8888
