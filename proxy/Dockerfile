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

RUN npm install && \
    npm run build && \
    node_modules/.bin/obfuscator --root lib/server/ \
       --entry lib/server/server.js \
       $(find lib/ -name "*.js") \
       $(find lib/ -name "*.json") \
       --out server.js.obfuscated && \
    # TODO Obfuscation should occur before ever adding js files as docker layer (or we should squash some layers)
    # Someone might just remove those two layers and build his own proxy docker image with source code.
    rm -rf app lib

RUN chmod +x server.js.obfuscated

CMD [ "npm", "start"]

