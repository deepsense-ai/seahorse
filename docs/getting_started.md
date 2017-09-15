---
layout: global
menuTab: getting_started
description: Seahorse Getting Started Page
title: Getting Started
---

**Table of Contents**

* Table of Contents
{:toc}

## Quick Introduction

Seahorse is a visual framework letting users create
<a target="_blank" href="https://spark.apache.org/">Apache Spark</a> applications in a intuitive and interactive way.
All while connected to any Spark Cluster (YARN, Mesos, Standalone) or to a bundled local Spark.

For a more detailed overview go to the [Overview](./index.html) section.

## Run Seahorse Standalone on Your Machine

#### Mac or Windows

Seahorse for Mac
<img class="img-responsive" style="display: inline-block; width:auto; height:15px;" src="./img/os_icons/osx.png">
and Windows
<img class="img-responsive" style="display: inline-block; width:auto; height:15px;" src="./img/os_icons/windows.png">
is distributed in a form of Vagrant image.

1. Install **Vagrant (required)**. You can find the Vagrant installation guide at [vagrantup.com](https://www.vagrantup.com/docs/installation/).
2. Download `Vagrantfile` from [get Seahorse page](http://deepsense.io/get-seahorse/).
3. Go to the catalog with `Vagrantfile` file and run `vagrant up` from the command line.
4. Go to <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">{{ site.SEAHORSE_EDITOR_ADDRESS }}</a> and start using Seahorse!

For more details and troubleshooting go to [Seahorse Standalone Deployment mode page](./deployment/standalone.html#seahorse-standalone-as-a-vagrant-image).

#### Linux

Seahorse for Linux
<img class="img-responsive" style="display: inline-block; width:auto; height:15px;" src="./img/os_icons/linux.png">
is distributed in a form of docker images.

1. Install **Docker (required)** and **docker-compose (required)**. You can find Docker installation guide at [docs.docker.com/engine](https://docs.docker.com/engine/installation/)
  and docker-compose installation guide at [docs.docker.com/compose](https://docs.docker.com/compose/install/).
2. Download `docker-compose.yml` from [get Seahorse page](http://deepsense.io/get-seahorse/).
3. Go to the catalog with `docker-compose.yml` file and run `docker-compose up` from the command line.
4. Go to <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">{{ site.SEAHORSE_EDITOR_ADDRESS }}</a> and start using Seahorse!

For more details and troubleshooting go to [Seahorse Standalone Deployment mode page](./deployment/standalone.html#dockerized-seahorse-standalone).

## Use Seahorse

In the following steps we will read some data.
Then we will apply a simple transformation to the data.

#### Create New Workflow and Read Your Data

* Run Seahorse on your machine and go to <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">{{ site.SEAHORSE_EDITOR_ADDRESS }}</a>.

<div class="align-left">
    <div class="img-responsive image-with-caption-container" style="width: 700px">
        <img class="img-responsive bordered-image" src="./img/seahorse_main.png">
        <em>Home screen of Seahorse is a list of all workflows - initially filled with examples.</em>
    </div>
</div>

* Create the new workflow using the **New Workflow** button. Workflow Editor will start.

<div class="align-left">
    <div class="img-responsive image-with-caption-container" style="width: 700px">
        <img class="img-responsive bordered-image" src="./img/getting_started/editor_empty_workflow.png">
        <em>Workflow Editor</em>
    </div>
</div>

* Start editing by clicking
  <img class="img-responsive" style="display: inline-block; width:auto; height:15px;" src="./img/getting_started/start_editing.png" />
  from the top menu. It will start up an Apache Spark backend for your workflow session.

* Read some data into Seahorse:
  * Drag **Read Dataframe** from the left-hand side panel onto canvas.
  * Click on the dragged **Read Dataframe** and change its parameters using the right-hand side panel.
    Set `source` parameter to
    <code>https://s3.amazonaws.com/workflowexecutor/examples/data/transactions.csv</code>

<!--This screen is needed because 'Library' icon visually separates `SOURCE` -->
<!-- and text input and it can be hard for user to know at once where should he input the text -->
<img class="align-left img-responsive bordered-image" style="width:300px; height:auto" src="./img/getting_started/source_param.png" />

* Run the operation by clicking
  <img class="img-responsive" style="display: inline-block; width:auto; height:15px;" src="./img/getting_started/run.png" />
  button from the top menu.
* Click <img class="img-responsive" style="display: inline-block; width:auto; height:15px;" src="./img/getting_started/report_icon.png" /> 
  on the operation node to preview Dataframe.


<div class="align-left">
    <div class="img-responsive image-with-caption-container" style="width: 600px">
        <img class="img-responsive bordered-image" src="./img/getting_started/transactions_sample.png">
        <em>DataFrame Report opened after clicking report icon</em>
    </div>
</div>

#### Transform Your Data

In the next step you will apply a simple transformation to our data.

* Drag a `Filter Columns` operation onto canvas.
* Drag the Dataframe produced by `Read Dataframe` operation to the `Filter Columns` operation.

<img class="align-left img-responsive" style="width:250px; height:auto" src="./img/getting_started/dragging_dataframe.png" />

* Set `selected column` parameter of `Filter Columns` to some set of columns of your choice.
* Run your workflow again and view the transformed Dataframe produced by `Filter Columns`!

<div class="align-left">
    <div class="img-responsive image-with-caption-container" style="width: 600px">
        <img class="img-responsive bordered-image" src="./img/getting_started/transactions_sample_after_filtering.png">
        <em>DataFrame Report with filtered columns</em>
    </div>
</div>

Congratulations! You have successfully created your first Seahorse workflow.

## Learn More!

* Check out the [basic examples](./basic_examples.html) or [some of the more advanced use cases](./casestudies/income_predicting.html)
* Check out [a full operation list with a documentation](./operations.html).
