---
layout: global
displayTitle: Seahorse in Server Mode
menuTab: reference
title: Server Mode
description: Seahorse in Server Mode
---

[Dockerized Seahorse](../deployment.html#dockerized-seahorse)
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
    ports:
      - 127.0.0.1:33321:33321
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
    ports:
      - 0.0.0.0:33321:33321
 ...
{% endhighlight %}
</div>
</div>

Additionally, to start Seahorse in daemon mode, you can run `docker-compose` with `-d` flag:

{% highlight bash %}
docker-compose up -d
{% endhighlight %}

This will start containers with Seahorse in the background.

Seahorse provides multi-user capability, useful in server mode. Multi-user allows creating workflows protected from modifications by other users.

Authorization service offers registration page to create a new account confirmed by sending an activation email. You have to log on to an account before using Seahorse.

To enable the authorization service, the `docker-compose.yml` file should be modified as described below.

Admin's password is 'admin' by default.
It can be reset normally - on the login screen.
Link for changing the password will be sent to the address provided in `docker-compose.yml`.


<div class="flex-adaptable-row-container">
<div class="flex-adaptable-column-container">
<b>Default Configuration</b>
{% highlight YAML %}
services:
  ...
  authorization:
    ...
    environment:
      ENABLE_AUTHORIZATION: 'false'

  ...
  proxy:
  ...
    environment:
      ...
      ENABLE_AUTHORIZATION: 'false'
 ...
{% endhighlight %}
</div>

<div class="flex-adaptable-column-container">
<b>Default authorization turned on</b>
{% highlight YAML %}
services:
  ...
  authorization:
    ...
    environment:
      ENABLE_AUTHORIZATION: 'true'
      SEAHORSE_ADMIN_EMAIL: {your mail here}
  ...
  proxy:
  ...
    environment:
      ...
      ENABLE_AUTHORIZATION: 'true'
 ...
{% endhighlight %}
</div>
</div>

To learn more about using Seahorse in production and such features as security, additional authorization methods (LDAP, Google, etc.) or custom deployment requirements,
please <a target="_blank" href="https://deepsense.ai/contact">contact us for details</a>.

{% include contact_box.html %}
