# Copyright (c) 2016, CodiLime Inc.

FROM mhart/alpine-node

RUN apk add git --update-cache

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY package.json /usr/src/app/
RUN npm install

COPY . /usr/src/app

EXPOSE 8080
CMD [ "npm", "start" ]

