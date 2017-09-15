---
layout: global
menuTab: downloads
description: Seahorse downloads page
title: Seahorse Desktop
---

**Table of Contents**

* Table of Contents
{:toc}

## Getting Started

Seahorse Desktop is one of Seahorse's distribution modes. Its purpose is to give you a taste of how
it is to work with Seahorse. This is why it's a self-contained virtualized environment that you can
quickly set up and get started.

Seahorse Desktop works on multiple platforms, including
<img src="img/os_icons/windows.png" alt="Windows" height="18" width="18"> Windows,
<img src="img/os_icons/osx.png" alt="OS X" height="18" width="18"> OS X and
<img src="img/os_icons/linux.png" alt="Linux" height="18" width="18"> Linux.
It comes in two flavours, functionally identical, but differing in terms of
requirements imposed on your operating system. Presented below are the two options.

### Dockerized Seahorse Desktop

This method of deployment uses a set of Docker containers under the hood and Docker Compose to
connect them with each other. Docker is a lightweight, secure containerization tool that allows
installing a complex application along with its dependencies without cluttering your OS.

If you're unsure which option fits you best, we suggest this one.

**Prerequisites**

* <a target="_blank" href="https://www.docker.com/">Docker</a> (tested on versions 1.10.3 and
1.11.2)
  * Docker Toolbox is not supported.
* <a target="_blank" href="https://docs.docker.com/compose/">Docker Compose</a> (tested on versions
1.7.0 and 1.7.1)
* <a target="_blank" href="https://www.google.com/chrome/">Google Chrome</a> (version >= 40)
or <a target="_blank" href="https://www.mozilla.org/firefox/">Mozilla Firefox</a> (version >= 48)

**Running Seahorse**

1. <a target="_blank" href="https://deepsense.io/get-seahorse/">Download</a>
the `docker-compose.yml` file, which serves as a config for Docker Compose (this may take a
few minutes).
2. Run `docker-compose up` in the directory containing the downloaded file.
3. When Seahorse Desktop is ready, open it in your browser. It is accessible at
    <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">{{ site.SEAHORSE_EDITOR_ADDRESS }}
    </a>

### Seahorse Desktop as a Vagrant Image

Despite being a mature technology, on some systems, Docker is still quite new. If you feel more
comfortable with a fully virtualized environment, we offer Seahorse Desktop wrapped inside a Vagrant
image.

**Prerequisites**

* <a target="_blank" href="https://www.vagrantup.com/">Vagrant</a> (tested on version 1.8.1)
* <a target="_blank" href="https://www.virtualbox.org/">VirtualBox</a> (tested on version 5.0.10,
there is an issue with version 5.0.12)
* <a target="_blank" href="https://www.google.com/chrome/">Google Chrome</a> (version >= 40)
or <a target="_blank" href="https://www.mozilla.org/firefox/">Mozilla Firefox</a> (version >= 48)

**Running Seahorse**

1. <a target="_blank" href="https://deepsense.io/get-seahorse/">Download</a>
the Vagrantfile that will allow you to spin up Seahorse’s virtual machine.
2. Run `vagrant up` in the directory containing `Vagrantfile`. This may take a few minutes.
3. When Seahorse Desktop is ready, go to locally hosted
<a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">{{ site.SEAHORSE_EDITOR_ADDRESS }}</a>
in your browser.

### Accessing Local Files in Seahorse

If you want to use data sets stored as local files, you need to copy them to the `data` directory
where you started Seahorse Desktop. They will be available in Seahorse at the `/resources/data/`
path.

So, for example, let's say Seahorse Desktop has been started in the `/tmp/seahorse/` directory.
A file `/tmp/seahorse/data/my_data.csv` will be visible in Seahorse as
`/resources/data/my_data.csv`.

## General Information

### Bundled Software Packages

* Apache Spark, version {{ site.WORKFLOW_EXECUTOR_SPARK_VERSION }}
* Python, version 2.7.6
* NumPy, version 1.8.2
* JDBC drivers:

    -   MySQL, version 5.1.38
    -   PostgreSQL, version 9.4.1207

### Minimum Hardware Requirements
* 6 GB of a free disk space
* 6 GB of RAM (Please note, that if you're running Seahorse Desktop as a Vagrant Image, or
inside a Docker Machine, the VM has to have this amount of memory available).
* A processor with virtualization support

### Seahorse Desktop Limitations

* Max DataFrame size: 500 MB
* Max number of rows in a DataFrame: 4 M
* Max number of columns in a DataFrame: 100
* Max row size: 1 MB

Above limitations refer only to Seahorse Desktop.
To learn more about scaling up and using Seahorse in production,
see the <a href="https://seahorse.deepsense.io/enterprise.html">Enterprise</a> page.


<div class="contact-block">
	<div class="contact-info">
		<p>Learn more about Seahorse enterprise-scale deployments
		- includes customized set-up, integration and 24/7 support.</p>
	</div>
	<div class="contact-block-container">
		<div class="contact-block-button">
			<a target="_blank" href="http://deepsense.io/about-us/contact/#contact-form-anchor">
			Contact us for details!
			</a>
		</div>
	</div>
</div>

## Upgrading To the Latest Version

If you want to replace your Seahorse Desktop with the newest version you need to invoke
the commands below. ***Please keep in mind that the current version of Seahorse will be
completely erased, meaning that all workflows stored in Seahorse will be deleted, too.***

Note, that all commands need to be executed in the directory containing either the
`docker-compose.yml` or `Vagrantfile` file, depending on which flavour of Seahorse Desktop you're
using.

### Dockerized Seahorse Desktop

{% highlight bash %}
# stop and delete current Seahorse
docker-compose down
# upgrade Docker images to the latest version
docker-compose pull
# run the upgraded Seahorse
docker-compose up
{% endhighlight %}

### Seahorse Desktop as a Vagrant Image

{% highlight bash %}
# stop and delete current Seahorse containers
vagrant destroy
# remove current seahorse box
vagrant box remove seahorse-vm
# remove older Vagrantfile
rm Vagrantfile
# get the newest Vagrantfile (please check Downloads page)
# wget is a simple command-line network downloader,
# but you can also download Vagrant file using a web browser
wget http://path.to.the.newest.vagrantfile
# start the newest Seahorse
vagrant up
{% endhighlight %}

## Troubleshooting
* If you cannot start Seahorse virtual machine, please check if virtualization is enabled in BIOS.
* In case of unexpected errors during Seahorse usage, try rebooting Dockerized Seahorse by running:

        docker-compose stop
        docker-compose up

    Or, if you're using Vagrant:

        vagrant reload

Bug reports and all other feedback can be sent using the
<a target="_blank" href="http://feedback.seahorse.deepsense.io">Feedback</a>
option in Seahorse Editor in the upper-right corner.

### Conflicting Ports
Seahorse, by default listens on port `{{ site.SEAHORSE_EDITOR_PORT }}`. In rare cases this
configuration may conflict with services running on your computer. This can be easily remedied by
replacing `{{ site.SEAHORSE_EDITOR_PORT }}` with a port of your choosing in `docker-compose.yml` or
`Vagrantfile` (depending on which flavour of Seahorse Desktop you're using). After restarting
Seahorse Desktop, it will be available under the address with the new port.

### Note for Windows users

By default, Vagrant keeps boxes and configuration in `%HOMEPATH%/.vagrant.d`,
but cannot access it when a `%HOMEPATH%` includes non-ASCII characters.
It is a well known
<a target="_blank" href="https://github.com/mitchellh/vagrant/issues/4966">Vagrant bug</a>
and results in:

    > vagrant up
    ...
    Failed writing body (0 != 16383)

There are at least two ways to overcome the problem:

* Change user name to one with ASCII characters only
* Change Vagrant home directory to one with ASCII characters only:

        setx VAGRANT_HOME c:\.vagrant.d
