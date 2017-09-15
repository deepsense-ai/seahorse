# Copyright (c) 2016, CodiLime Inc.
FROM docker-repo.deepsense.codilime.com/intel/tap-base-node:node4.4-jessie

RUN apt-get update && \
    apt-get install --no-install-recommends --no-install-suggests -y git && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY package.json /usr/src/app/
RUN npm install

COPY . /usr/src/app

CMD [ "npm", "start" ]

