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
platform that allows you to create Spark applications using web-based, interactive user interface.
Seahorse bundled image is a virtual machine containing all necessary components to easily run Seahorse.

<img class="img-responsive" src="./img/bundled_image_overview.png" />

### Bundled JDBC drivers
Seahorse supports databases compliant with JDBC. The Bundled Image includes JDBC drivers for:

*   MySQL, version 5.1.38
*   PostgresSQL, version 9.4.1207

## Run Seahorse Bundled Image

### Requirements
* <a target="_blank" href="https://www.vagrantup.com/">Vagrant</a> (tested on version 1.8.1)
* <a target="_blank" href="https://www.virtualbox.org/">VirtualBox</a> (tested on version 5.0.10, there is an issue with version 5.0.12)
* <a target="_blank" href="https://www.google.com/chrome/">Google Chrome</a> (version >= 41)

### Download Seahorse Bundled Image

Vagrantfile describing image containing all necessary components of Seahorse can be downloaded from
[Downloads page](/downloads.html).
Seahorse bundled image is based on <a target="_blank" href="http://www.ubuntu.com/">Ubuntu</a> and contains Spark distribution in it.
Currently in the Seahorse community edition there is no possibility to connect to an external Spark cluster.

### Run Command
To run Seahorse bundled image you have to navigate to the directory where you downloaded the Vagrantfile and execute:

    vagrant up

After that you can navigate to <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">Seahorse Editor</a>.

### Shutdown Command
To stop Seahorse bundled image you need to execute:

    vagrant halt

## Making Local Files Available in Seahorse
By default Vagrant mounts host directory where virtual machine was started to `/vagrant` directory on virtual machine.
If you want to use data sets stored as local files you need to copy them to the directory where you started your virtual machine.

Example:

    cp /path/to/the/dataset/data.csv /path/to/the/dir/where/vagrantfile/is/

<img class="img-responsive" src="./img/file_param.png" />

## Troubleshooting
If something went wrong during Seahorse exploration, please let us know about faulty behaviour using
"Feedback" option in Seahorse Editor and restart your Seahorse:

    vagrant reload

## Replacing Seahorse Bundled Image With the Latest Version
If you want to replace your Seahorse with the newest version you need to invoke
the commands below. Please keep in mind that the current version of Seahorse will be
completely erased, meaning that all workflows stored in Seahorse will be deleted, too.
{% highlight bash %}
# stop and delete current Seahorse
vagrant destroy
# remove current seahorse box
vagrant box remove seahorse-vm
# remove older Vagrantfile
rm Vagrantfile
# get the newest Vagrantfile (please check Downloads page)
wget http://path.to.the.newest.vagrantfile
# start the newest Seahorse
vagrant up
{% endhighlight %}
