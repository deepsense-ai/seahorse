---
layout: documentation
displayTitle: Create k-means Clustering
title: Create k-means Clustering
description: Create k-means Clustering
usesMathJax: true
includeOperationsMenu: true
---

Creates an [UntrainedKMeansClustering](../classes/untrained_k_means_clustering.html) model.

The training algorithm initializes cluster centers using one of two methods: ``k-means||`` or
``random``. After initialization, the following two steps are executed in a loop until
the stop condition is satisfied:

1. Every observation in the input [DataFrame](../classes/dataframe.html) is assigned to the nearest
cluster center (by Euclidean distance).
2. Cluster centers are moved to geometric centers of assigned observations.

The stop condition is satisfied when one of the following occurs:

* Every cluster center is moved by no more than $$\varepsilon$$.
* Number of iterations has exceeded the given maximum.

For more details about ``k-means||`` initialization method, read:
[http://spark.apache.org/docs/latest/mllib-clustering.html#k-means](http://spark.apache.org/docs/latest/mllib-clustering.html#k-means)

The output model is eligible for [training](train_clustering.html).

**Since**: Seahorse 0.5.0

## Input

Create k-means Clustering does not take any input.

## Output

<table>
  <thead>
    <tr>
      <th style="width:25%">Port</th>
      <th style="width:25%">Type Qualifier</th>
      <th style="width:50%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <code>0</code>
      </td>
      <td>
        <code><a href="../classes/untrained_k_means_clustering.html">UntrainedKMeansClustering</a></code>
      </td>
      <td>An untrained model</td>
    </tr>
  </tbody>
</table>

## Parameters

<table class="table">
  <thead>
    <tr>
      <th style="width:25%">Name</th>
      <th style="width:25%">Type</th>
      <th style="width:50%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>number of clusters</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Number of clusters.</td>
    </tr>
    <tr>
      <td><code>max iterations</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Maximum number of iterations.</td>
    </tr>
    <tr>
      <td><code>initialization mode</code></td>
      <td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
      <td>Initialization mode of cluster centers.
      Possible values: <code>["k-means||", "random"]</code>.</td>
    </tr>
    <tr>
      <td><code>initialization steps</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Available only if <code>initialization mode</code> is set to <code>"k-means||"</code>.
      Number of initialization steps.</td>
    </tr>
    <tr>
      <td><code>seed</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Seed for cluster initialization.</td>
    </tr>
    <tr>
      <td><code>number of runs</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Number of runs of the algorithm to execute in parallel.
      We initialize the algorithm this many times with random starting conditions,
      then return the best clustering found.</td>
    </tr>
    <tr>
      <td><code>epsilon</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>The distance threshold within which we consider centers to have converged.</td>
    </tr>
  </tbody>
</table>
