---
layout: documentation
displayTitle: Seahorse Bundled Image
docTab: bundled_image_overview
title: Seahorse Bundled Image
includeTechnicalOverviewMenu: true
description: Seahorse Bundled Image
---


**Table of Contents**

* Table of Contents
{:toc}

## Overview

Seahorse is an <a target="_blank" href="http://spark.apache.org">Apache Spark</a>
platform that allows you to create Spark applications using a web-based interactive user interface.
Seahorse Bundled Image is a virtual machine containing all necessary components to easily run Seahorse.
The purpose of Seahorse Bundled Image is to present important features of Seahorse rather than
production use, this is why it consists of the simplest system setup which does not support Big Data.

<img class="img-responsive" src="./img/bundled_image_overview.png" />

### Seahorse Bundled Image Limitations

* Max DataFrame size: 500 MB
* Max number of rows in a DataFrame: 4 M
* Max number of columns in a DataFrame: 100
* Max cell size: 1 MB

<div class="contact-block">
	<div>
		<p class="contact-block-info">Learn more about Seahorse enterprise-scale deployments
		<br>- includes customized set-up, integration and 24/7 support.</p>
	</div>
	<div>
		<button class="contact-block-button">
			<a href="http://deepsense.io/about-us/contact/">
			Contact us for pricing details!
			</a>
		</button>
	</div>
</div>

### Bundled Dependencies

* Apache Spark, version 1.6.0
* Python, version 2.7.6
* NumPy, version 1.8.2
* JDBC drivers:

    -   MySQL, version 5.1.38
    -   PostgreSQL, version 9.4.1207


## Run Seahorse Bundled Image

### Minimum Hardware Requirements
* 4 GB of a free disk space
* 4 GB of RAM
* A processor with virtualization support

### Software Requirements
* <a target="_blank" href="https://www.vagrantup.com/">Vagrant</a> (tested on version 1.8.1)
* <a target="_blank" href="https://www.virtualbox.org/">VirtualBox</a> (tested on version 5.0.10, there is an issue with version 5.0.12)
* <a target="_blank" href="https://www.google.com/chrome/">Google Chrome</a> (version 40+)

### Download Seahorse Bundled Image

Vagrantfile describing image containing all necessary components of Seahorse can be downloaded from
[Downloads Page](/downloads.html).
Seahorse Bundled Image is based on <a target="_blank" href="http://www.ubuntu.com/">Ubuntu</a> and contains Spark distribution in it.

### Run Command
To run Seahorse Bundled Image you have to navigate to the directory where you downloaded the Vagrantfile and execute:

    vagrant up

After that you can navigate to <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">Seahorse Editor</a>.

### Shutdown Command
To stop Seahorse Bundled Image you need to execute:

    vagrant halt

## Making Local Files Available in Seahorse
By default Vagrant mounts host directory where virtual machine was started to `/vagrant` directory on virtual machine.
If you want to use data sets stored as local files you need to copy them to the directory where you started your virtual machine.

Example:

    cp /path/to/the/dataset/data.csv /path/to/the/dir/where/vagrantfile/is/

<img class="img-responsive" src="./img/file_param.png" />

## Troubleshooting
* If you cannot start Seahorse virtual machine, please check your BIOS if you have virtualization enabled.
* If something went wrong during Seahorse exploration, please let us know about faulty behaviour using
"Feedback" option in Seahorse Editor and restart your Seahorse:

        vagrant reload

### Note for Windows users

By default, Vagrant keeps boxes and configuration in `%HOMEPATH%/.vagrant.d`,
but cannot access it when a `%HOMEPATH%` includes non-ASCII characters.
It's a well known <a target="_blank" href="https://github.com/mitchellh/vagrant/issues/4966">Vagrant bug</a> and results in:

    > vagrant up
    ...
    Failed writing body (0 != 16383)

There are at least two ways to overcome the problem:

* Change user name to one with ASCII characters only
* Change Vagrant home directory to one with ASCII characters only:

        setx VAGRANT_HOME=c:\.vagrant.d

## Replacing Seahorse Bundled Image With the Latest Version
If you want to replace your Seahorse with the newest version you need to invoke
the commands below. ***Please keep in mind that the current version of Seahorse will be
completely erased, meaning that all workflows stored in Seahorse will be deleted, too.***
{% highlight bash %}
# stop and delete current Seahorse
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
