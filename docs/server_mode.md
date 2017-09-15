---
layout: global
displayTitle: Seahorse in Server Mode
menuTab: reference
title: Server Mode
description: Seahorse in Server Mode
---

[Dockerized Seahorse Standalone](deployment/standalone.html#dockerized-seahorse-standalone)
is designed to work as a server application,
which allows multiple users to utilize a single instance of Seahorse.

Enabling Server Mode comes down to telling Seahorse the IP address it should be available at.
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
    environment:
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
    environment:
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

To learn more about using Seahorse in production and such features as security and custom deployment requirements,
see the [Enterprise](deployment/enterprise.html) page.

{% include contact_box.html %}
