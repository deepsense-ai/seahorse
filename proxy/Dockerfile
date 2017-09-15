# Copyright (c) 2016, CodiLime Inc.
FROM docker-repo.deepsense.codilime.com/intel/tap-base-node:node4.4-jessie

RUN apt-get update && \
    apt-get install --no-install-recommends --no-install-suggests -y git && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/docker
WORKDIR /opt/docker

COPY package.json .
RUN npm install

COPY . .
# TODO Building should occur before ever adding js files as docker layer (or we should squash some layers)
# Otherwise someone might access source code
RUN npm run build && \
    rm -rf app

RUN chmod +x start_proxy.sh
CMD ["./start_proxy.sh"]
