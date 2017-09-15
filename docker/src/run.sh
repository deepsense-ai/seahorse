#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.

render_template() {
  eval "echo \"$(cat $1)\""
}

echo "Generating config from template"
render_template /tmp/config.js.tmpl > /usr/share/nginx/html/config.js

echo "Starting..."
exec "$@"
