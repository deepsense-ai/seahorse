#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.
#
# Builds frontend with all the dependencies

function prepare_environment() {
  echo "** Preparing Environment **"
  # this returns an error unless it's run for the first time on a machine
  # we know about it and want to continue anyway
  npmrc -c codilime || test 1
  npmrc codilime
  npm set registry $NPM_REGISTRY_URL

  sudo npm install -g webpack
  sudo npm install -g npmrc
  npm install
}

function build() {
  echo "** Building package **"
  npm run dist
}

prepare_environment
build
