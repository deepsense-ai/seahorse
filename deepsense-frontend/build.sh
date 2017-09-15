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

  sudo npm install -g gulp
  sudo npm install -g npmrc

  #install all components dependencies
  (cd ../deepsense-components && ./install_all.sh)
  #build all components
  (cd ../deepsense-components && ./build_all.sh)
  npm install
}

function build() {
  echo "** Building package **"
  gulp clean
  gulp build
}

prepare_environment
build
