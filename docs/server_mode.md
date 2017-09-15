---
layout: global
displayTitle: Seahorse in Server Mode
menuTab: reference
title: Server Mode
description: Seahorse in Server Mode
---

[Dockerized Seahorse Standalone](deployment/standalone.html#dockerized-seahorse-standalone)
is designed to work as a server application,
which allows multiple users to access a single instance of Seahorse.

Enabling Server Mode requires telling Seahorse the IP address it should be available at.
By default, it listens only for local connections. Below you can find a comparison of relevant portions of the
`docker-compose.yml` file with the assumption that Seahorse should be accessible under all IP addresses
of the machine.

<div class="flex-adaptable-row-container">
<div class="flex-adaptable-column-container">
<b>Default Configuration</b>
{% highlight YAML %}
services:
  ...
  proxy:
  ...
    environment: &proxy_environment_value
      ...
      HOST: "127.0.0.1"
 ...
{% endhighlight %}
</div>

<div class="flex-adaptable-column-container">
<b>Server Mode</b>
{% highlight YAML %}
services:
  ...
  proxy:
  ...
    environment: &proxy_environment_value
      ...
      HOST: "0.0.0.0"
 ...
{% endhighlight %}
</div>
</div>

Additionally, to start Seahorse in daemon mode, you can run `docker-compose` with `-d` flag:

{% highlight bash %}
docker-compose up -d
{% endhighlight %}

This will start containers with Seahorse in the background.

Seahorse provides multi user support, useful in server mode. Multi user allows creating workflows protected from modifications by other users. You can either use default authorization service or connect to your authorization service (LDAP, Google, etc.).

Default authorization service offers registration page to create new account by sending an activation email. You have to log on to an account before using Seahorse.

To enable the internal authorization service, the `docker-compose.yml` file should be modified as described below.

<div class="flex-adaptable-row-container">
<div class="flex-adaptable-column-container">
<b>Default Configuration</b>
{% highlight YAML %}
services:
  ...
  proxy:
  ...
    environment: &proxy_environment_value
      ...
      ENABLE_AUTHORIZATION: "false"
 ...
{% endhighlight %}
</div>

<div class="flex-adaptable-column-container">
<b>Default authorization turned on</b>
{% highlight YAML %}
services:
  ...
  proxy:
  ...
    environment: &proxy_environment_value
      ...
      ENABLE_AUTHORIZATION: "true"
 ...
{% endhighlight %}
</div>
</div>


To learn more about using Seahorse in production and such features as security, additional authorization methods and custom deployment requirements,
please <a target="_blank" href="http://deepsense.io/about-us/contact/#contact-form">contact us for details</a>.

{% include contact_box.html %}
