---
layout: global
menuTab: deployment
description: Seahorse Deployment Overview
title: Seahorse Deployment
---

**Table of Contents**

* Table of Contents
{:toc}

## Overview

You can install and run Seahorse on your local computer
or set it up in a server mode (for details see [Server Mode](reference/server_mode.html) section).

Seahorse
* works with self-contained local Spark;
* can connect to any external Spark cluster (YARN, Mesos or Spark Standalone);
* works on multiple platforms, including
<img src="img/os_icons/windows.png" alt="Windows" height="18" width="18"> Windows,
<img src="img/os_icons/osx.png" alt="OS X" height="18" width="18"> OS X and
<img src="img/os_icons/linux.png" alt="Linux" height="18" width="18"> Linux.

If you want to install Seahorse on <img src="img/os_icons/linux.png" alt="Linux" height="18" width="18"> Linux
you should use [Dockerized Seahorse](#dockerized-seahorse).

If you want to install Seahorse on
<img src="img/os_icons/osx.png" alt="OS X" height="18" width="18"> OS X or
<img src="img/os_icons/windows.png" alt="Windows" height="18" width="18"> Windows
you should use [Seahorse as a Vagrant Image](#seahorse-as-a-vagrant-image).

You can also build Seahorse from source code. Follow instructions at
<a target="_blank" href="https://github.com/deepsense-ai/seahorse/">
Seahorse github repo
</a>.

## Dockerized Seahorse

**Works on:** <img src="img/os_icons/linux.png" alt="Linux" height="18" width="18"> Linux

### Installation

This method of deployment uses a set of Docker containers and Docker Compose to
connect them with each other. Docker is a lightweight, secure containerization tool that allows
installing a complex application along with its dependencies without cluttering your OS.

**Prerequisites**

* <a target="_blank" href="https://www.docker.com/">Docker</a> (tested on versions 1.10.3,
1.11.2 and 1.12.0)
  * Docker Toolbox and Docker on Mac are not supported.
* <a target="_blank" href="https://docs.docker.com/compose/">Docker Compose</a> (tested on versions
1.7.0 and 1.7.1)
* <a target="_blank" href="https://www.google.com/chrome/">Google Chrome</a> (version >= 51)
or <a target="_blank" href="https://www.mozilla.org/en-US/firefox/">Mozilla Firefox</a> (version >= 48)

**Running Seahorse**

1. <a target="_blank" href="https://get-seahorse.deepsense.ai/">Download</a>
the `docker-compose.yml` file, which serves as a config for Docker Compose (this may take a
few minutes).
2. Run `docker-compose up` in the directory containing the downloaded file.
3. When Seahorse is ready, open it in your browser. It is accessible at
    <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">{{ site.SEAHORSE_EDITOR_ADDRESS }}.
    </a>

### Troubleshooting
* In case of unexpected errors during Seahorse usage, try rebooting Dockerized Seahorse by running:

        docker-compose down
        docker-compose up

* Bug reports and all other feedback can be sent using the
  <a target="_blank" href="https://community.seahorse.deepsense.ai">Feedback</a>
  option in the Seahorse Editor in the upper-left corner.

### Conflicting Ports

Seahorse, by default listens on port `{{ site.SEAHORSE_EDITOR_PORT }}`. In rare cases this
configuration may conflict with services running on your computer. This can be easily remedied by
replacing `{{ site.SEAHORSE_EDITOR_PORT }}` with a port of your choosing in `docker-compose.yml` file. After restarting
Seahorse, it will be available under the address with the new port.

<br />

## Seahorse as a Vagrant Image

**Works on:** <img src="img/os_icons/osx.png" alt="OS X" height="18" width="18"> OS X,
              <img src="img/os_icons/windows.png" alt="Windows" height="18" width="18"> Windows,
              <img src="img/os_icons/linux.png" alt="Linux" height="18" width="18"> Linux

### Installation

On some operating systems Docker is not supported natively.
That's why we also distribute Seahorse as a Vagrant
image.

**Prerequisites**

* <a target="_blank" href="https://www.vagrantup.com/">Vagrant</a> (tested on version 1.8.1)
* <a target="_blank" href="https://www.virtualbox.org/">VirtualBox</a> (tested on version 5.0.10,
there is an issue with version 5.0.12)
* <a target="_blank" href="https://www.google.com/chrome/">Google Chrome</a> (version >= 51)
or <a target="_blank" href="https://www.mozilla.org/en-US/firefox/">Mozilla Firefox</a> (version >= 48)

**Running Seahorse**

1. <a target="_blank" href="https://get-seahorse.deepsense.ai/">Download</a>
the Vagrantfile that will allow you to spin up Seahorse’s virtual machine.
2. Run `vagrant up` in the directory containing `Vagrantfile`. This may take a few minutes.
3. When Seahorse is ready, go to
<a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">{{ site.SEAHORSE_EDITOR_ADDRESS }}</a>
in your browser.

### Troubleshooting
* If you cannot start the Seahorse virtual machine, please check if virtualization is enabled in BIOS.
* In case of unexpected errors during Seahorse usage, try rebooting Seahorse by running:

        vagrant reload

* Bug reports and all other feedback can be sent using the
  <a target="_blank" href="https://community.seahorse.deepsense.ai">Feedback</a>
  option in the Seahorse Editor in the upper-left corner.


### Conflicting Ports
Seahorse, by default listens on port `{{ site.SEAHORSE_EDITOR_PORT }}`. In rare cases this
configuration may conflict with services running on your computer. This can be easily remedied by
replacing `{{ site.SEAHORSE_EDITOR_PORT }}` with a port of your choosing in `Vagrantfile`. After restarting
Seahorse, it will be available under the address with the new port.

### Notes for Windows Users

#### Problem with Special Characters in HOMEPATH Environment Variable

By default, Vagrant keeps boxes and configuration in `%HOMEPATH%/.vagrant.d`,
but cannot access it when a `%HOMEPATH%` includes non-ASCII characters.
It is a well known
<a target="_blank" href="https://github.com/mitchellh/vagrant/issues/4966">Vagrant bug</a>
and results in:

    > vagrant up
    ...
    Failed writing body (0 != 16383)

There are at least two ways to overcome the problem:

* Change the user name to one with ASCII characters only
* Change the Vagrant home directory to one with ASCII characters only:

        setx VAGRANT_HOME c:\.vagrant.d

<br />

#### Problem with Downloading Vagrant Box

Vagrant fails to download Seahorse Box file, but does not print any specific error message.

    An error occurred while downloading the remote file. The error
    message, if any, is reproduced below. Please fix this error and try
    again.

To overcome this
<a target="_blank" href="https://github.com/mitchellh/vagrant/issues/6764">issue</a>,
install
<a target="_blank" href="http://www.microsoft.com/en-us/download/confirmation.aspx?id=8328">Microsoft Visual C++ 2010 SP1 Redistributable Package (x86)</a>.

## Seahorse in Production Environment

To learn more about scaling up and using Seahorse in production,
please <a target="_blank" href="https://deepsense.ai/contact">contact us for details</a>.

{% include contact_box.html %}
