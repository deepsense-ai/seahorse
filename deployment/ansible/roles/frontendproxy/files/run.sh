#!bin/bash

# Copyright (c) 2015, CodiLime Inc.

cd ..
eval "echo \"$(<config.json)\"" > build/server/config.json
cd build
cat server/config.json
npm run start >> frontend-proxy.out 2>> frontend-proxy.err
