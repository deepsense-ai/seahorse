#!/usr/bin/env bash

# This script is used by our CI (Jenkins) to run unit tests job.
npm set registry http://10.10.1.124:4873
npm install
gulp build --ci
npm run test -- --single-run
