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

Seahorse is a powerful visual editor allowing you to create
<a target="_blank" href="https://spark.apache.org/">Apache Spark</a> applications
in a fast, simple and interactive way. Creating Apache Spark applications
with Seahorse is as easy as drag and dropping operation blocks.
All while connected to any Spark Cluster (YARN, Mesos, Standalone) or to a bundled local Spark.

<div class="centered-container spacer" markdown="1">
  <img src="img/seahorse_editor.png"
    class="centered-image img-responsive spacer" style="width:90%"/>
  *Seahorse's interface*
</div>

#### A Glimpse of Seahorse's Features

* Create Apache Spark application in a visual way using a web-based editor.
* Connect to any cluster (YARN, Mesos, Spark Standalone) out of a box or use the bundled local Spark.
* Use Seahorse Library to easily work with local files and the cluster.
* Use Spark's machine learning algorithms.
* Define custom operations with Python or R.
* Explore data with a Jupyter notebook using Python or R from within Seahorse.
* Export workflow and run it as a batch Apache Spark application using our Seahorse Batch Workflow Executor.

#### About the Product

Using Seahorse, you can solve complex problems in the areas of machine learning and ETL (Extract,
Transform and Load) without knowing Spark's internals. It provides tools to tackle real world
Big Data problems while letting the user experience a very gentle learning curve. Seahorse takes
care of many complicated concepts and presents a simple, clean interface.

Seahorse emphasizes a visual approach to programming. This results in user's applications being
extremely readable: the logic driving the entire program is visible at first glance.

What’s important, while promoting a code-free working style, Seahorse does not limit users to a
predefined set of actions. Whenever the user encounters a necessity to include a non-standard action
in their application - something that is not covered by Seahorse's palette of operations - they are
free to write their own transformations in
<a target="_blank" href="https://www.python.org/">Python</a>
and <a target="_blank" href="https://www.r-project.org/">R</a>.

Seahorse offers a web-based interface that presents a Spark application as a graph of operations -
a workflow. A typical Seahorse session consists of three alternating phases: adding operations
to the workflow, executing the part of it that's already been created and exploring the results
of the execution. This establishes an interactive process during which the user is able to track
what happens at each step.

Finally, after the workflow has been constructed, it can be exported and deployed as a typical Spark
application on production clusters.

#### Learn More

* [Getting Started](./getting_started.html)
* [Seahorse Operations List](./operations.html)
* [Example Use Case - Text Message Spam Detection](./casestudies/text_message_spam_detection.html)

{% include contact_box.html %}
