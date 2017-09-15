# Copyright (c) 2016, CodiLime Inc.
FROM  node:6.11-slim

RUN npm install webpack -g
RUN npm install webpack-node-externals --save-dev -g

RUN apt-get update && \
    apt-get install --no-install-recommends --no-install-suggests -y git && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/docker
WORKDIR /opt/docker

COPY package.json .
RUN npm config set registry http://registry.npmjs.org/ && npm install

COPY . .
# TODO Building should occur before ever adding js files as docker layer (or we should squash some layers)
# Otherwise someone might access source code
RUN npm run build && \
    rm -rf app

CMD ["npm", "start"]
