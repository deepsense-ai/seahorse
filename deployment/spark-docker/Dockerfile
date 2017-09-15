# Copyright (c) 2016, CodiLime Inc.
FROM ubuntu:16.04

# Install packages
# libcurl4-openssl-dev and libssl-dev are needed for IRkernel dependencies
# gfortran liblapack-dev liblapack3 libopenblas-base libopenblas-dev are needed for machine learning R libraries
RUN apt-get update && \
    apt-get install -y build-essential openjdk-8-jre wget curl bzip2 openssh-server openssh-client libsm6 \
    r-base libcurl4-openssl-dev libssl-dev && \
    apt-get install -y gfortran liblapack-dev liblapack3 libopenblas-base libopenblas-dev && \
    apt-get clean && \
    apt-get autoremove && \
    rm -rf /var/lib/apt/lists/*

RUN R --slave -e  "install.packages(c('repr', 'IRdisplay', 'pbdZMQ', 'devtools'), repos='http://cran.uni-muenster.de/')"
RUN R --slave -e  "devtools::install_github('IRkernel/IRkernel', ref='66e4fdda2e3cba3763bb45ac8b649dc73a3fa4bc')"

# Setup passwordless SSH
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa
RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

# Install Spark
ENV SPARK_VERSION 2.0.0
ENV SPARK_PACKAGE spark-$SPARK_VERSION-bin-hadoop2.7
ENV SPARK_HOME /opt/spark-$SPARK_VERSION
ENV PATH $PATH:$SPARK_HOME/bin
RUN wget -q -O - \
  "http://d3kbcqa49mib13.cloudfront.net/$SPARK_PACKAGE.tgz" \
  | gunzip \
  | tar x -C /tmp/ \
  && mv /tmp/$SPARK_PACKAGE $SPARK_HOME \
  && rm -rf $SPARK_HOME/examples $SPARK_HOME/ec2 \
  && rm -rf $SPARK_HOME/lib/spark-examples-2.0.0-hadoop2.7.0.jar

# Install conda
RUN wget -q -O /tmp/Miniconda2-latest-Linux-x86_64.sh \
    https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh && \
    bash /tmp/Miniconda2-latest-Linux-x86_64.sh -f -b -p /opt/conda && \
    rm /tmp/Miniconda2-latest-Linux-x86_64.sh && \
# Install python packages
    /opt/conda/bin/conda install --yes \
    'ipykernel=4.3.1' 'jupyter_client=4.3.0' 'pandas=0.16*' 'matplotlib=1.4*' \
    'scipy=0.15*' 'seaborn=0.6*' 'scikit-learn=0.16*' && \
    /opt/conda/bin/conda clean --all --yes

COPY startup.sh /opt/
RUN chown root.root /opt/startup.sh && chmod 700 /opt/startup.sh

ENTRYPOINT [ "/opt/startup.sh" ]
CMD [ "-d" ]
