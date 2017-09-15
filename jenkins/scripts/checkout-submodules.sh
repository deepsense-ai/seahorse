#!/usr/bin/env bash

# Assumption - this script is run from project root.

# Jenkins won't automatically update submodules.
rm -rf seahorse-workflow-executor;
git submodule init;
git submodule update;