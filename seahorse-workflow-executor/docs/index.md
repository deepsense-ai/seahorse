---
layout: global
displayTitle: Seahorse Overview
menuTab: overview
title: Seahorse Overview
description: Seahorse Overview
---

**Table of Contents**

* Table of Contents
{:toc}

#### Introduction

Seahorse is an open-source visual framework allowing you to create
<a target="_blank" href="https://spark.apache.org/">
  <img
    class="img-responsive"
    style="display: inline-block; width:auto; height:40px; margin-top: -11px;"
    alt="Apache Spark"
    src="./img/spark-logo.png">
</a>
applications in a fast, simple and interactive way. Creating Spark applications
with Seahorse is as easy as dragging and dropping operation on the canvas, all while
connected to any Spark Cluster (YARN, Mesos, Standalone) or to a bundled local Spark.

<div class="centered-container spacer" markdown="1">
  <img src="img/seahorse_editor.png"
    class="centered-image img-responsive spacer bordered-image" style="width:90%"/>
  *Seahorse's interface*
</div>

#### A Glimpse of Seahorse's Features

* Create Apache Spark applications in a visual way using a web-based editor.
* Connect to any cluster (YARN, Mesos, Spark Standalone) or use the bundled local Spark.
* Use the Seahorse Library to easily work with local files.
* Use Spark's machine learning algorithms.
* Define custom operations with Python or R.
* Explore data with a Jupyter notebook using Python or R from within Seahorse, sharing the same Spark context.
* Export workflows and run them as batch Apache Spark applications using the Batch Workflow Executor.

#### About the Product

Using Seahorse, you can create complex dataflows for ETL (Extract,
Transform and Load) and machine learning without knowing Spark's internals. Seahorse provides tools to tackle real world
Big Data problems while letting the user experience a very gentle learning curve. Seahorse takes
care of many complicated concepts and presents a simple, clean interface.

Seahorse emphasizes a visual approach to programming. This results in user's applications being
extremely readable: the logic driving the entire program is visible at first glance.

What’s important, while promoting a code-free working style, Seahorse does not limit users to a
predefined set of actions. Whenever the user encounters a necessity to include a non-standard action
in their application - something that is not covered by Seahorse's palette of operations - they can write their own transformations in
<a target="_blank" href="https://www.python.org/">Python</a>
and <a target="_blank" href="https://www.r-project.org/">R</a>.

Seahorse offers a web-based interface that presents a Spark application as a graph of operations -
a *workflow*. A typical Seahorse session consists of three alternating phases: adding operations
to the workflow, executing the part of it that's already been created and exploring the results
of the execution. This establishes an interactive process during which the user is able to track
what happens at each step.

Finally, after the workflow has been constructed, it can be exported and deployed as a standalone Spark
application on production clusters.

#### Learn More

* [Getting Started](./getting_started.html)
* [Seahorse Operations List](./operations.html)
* [Example Use Case - Text Message Spam Detection](./casestudies/text_message_spam_detection.html)
* [SDK User Guide](./reference/sdk_user_guide.html)
{% include contact_box.html %}
