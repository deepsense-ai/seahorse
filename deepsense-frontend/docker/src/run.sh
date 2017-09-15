render_template() {
  eval "echo \"$(cat $1)\""
}

echo "Generating config from template"
render_template /tmp/config.js.tmpl > /var/www/config.js

echo "Starting nginx"
nginx
