#!bin/bash

# Copyright (c) 2015, CodiLime Inc.
#
# Overrides config.js with content evaluated from environment variables

eval "echo \"$(<config.js)\"" > /usr/share/nginx/html/config.js

# Restart nginx after configuration change
service nginx restart

while true; do sleep 1000; done
