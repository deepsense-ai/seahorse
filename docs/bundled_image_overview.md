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
platform that allows user to create Spark applications using web-based, interactive user interface.
Seahorse bundled image is a virtual machine containing all necessary components to easily run Seahorse.

<img class="img-responsive" src="./img/bundled_image_overview.png" />

## Download Seahorse Bundled Image

Vagrantfile describing image containing all necessary components of Seahorse can be downloaded from
[Downloads page](/downloads.html).
Seahorse bundled image is based on Ubuntu 14.04.3 LTS and contains Spark distribution in it.
Currently in the Seahorse community edition there is no possibility to connect to an external Spark cluster.

## Run Seahorse Bundled Image

### Requirements
* <a target="_blank" href="https://www.vagrantup.com/">Vagrant</a> (tested on version 1.8.1)
* <a target="_blank" href="https://www.virtualbox.org/">VirtualBox</a> (tested on version 5.0.10, there is an issue with version 5.0.12)
* <a target="_blank" href="https://www.google.com/chrome/">Google Chrome</a>

### Run command
To run Seahorse bundled image user has to navigate to the directory where he downloaded Vagrantfile and execute: 

    vagrant up

After that user can navigate to <a target="_blank" href="http://localhost:8000">http://localhost:8000</a>
to fully experience Seahorse power.

User can log in to the virtual machine using (in directory where Vagrantfile is):

    vagrant ssh


## Making local files available in Seahorse
To use data sets stored as local files in Seahorse user has to copy these files to the directory where
Vagrantfile is. This will make files available in the /vagrant/ directory in Seahorse.

Example:

    cp /path/to/the/dataset/data.csv /path/to/the/dir/where/vagrantfile/is/

<img class="img-responsive" src="./img/file_param.png" />

