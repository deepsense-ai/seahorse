#!/usr/bin/env bash

# This script is used by our CI (Jenkins) to run unit tests job.
npm install
npm run test
