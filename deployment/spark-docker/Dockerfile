# Copyright (c) 2016, CodiLime Inc.
FROM sequenceiq/spark:1.6.0

RUN curl https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh -o /tmp/Miniconda2-latest-Linux-x86_64.sh
RUN bash /tmp/Miniconda2-latest-Linux-x86_64.sh -f -b -p /opt/conda

RUN /opt/conda/bin/conda install --yes notebook terminado
RUN /opt/conda/bin/conda install --yes 'ipython=4.0*' 'ipywidgets=4.0*' 'pandas=0.16*' 'matplotlib=1.4*' 'scipy=0.15*' 'seaborn=0.6*' 'scikit-learn=0.16*' pyzmq

COPY startup.sh /tmp/startup.sh
RUN chown root.root /tmp/startup.sh
RUN chmod 700 /tmp/startup.sh

ENTRYPOINT ["/tmp/startup.sh"]
