---
layout: global
displayTitle: LDA
title: LDA
description: LDA
usesMathJax: true
includeOperationsMenu: true
---
Latent Dirichlet Allocation (LDA), a topic model designed for text documents. LDA is given a
collection of documents as input data, via the `features column` parameter. Each document is
specified as a vector of length equal to the vocabulary size, where each entry is the count
for the corresponding term (word) in the document. Feature transformers such as Tokenize and
Count Vectorizer can be useful for converting text to word count vectors.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-clustering.html#latent-dirichlet-allocation-lda">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.clustering.LDA">org.apache.spark.ml.clustering.LDA documentation</a>.

**Since**: Seahorse 1.1.0

## Input

This operation does not take any input.

## Output


<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Type Qualifier</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
    <tr><td><code>0</code></td><td><code><a href="../classes/estimator.html">Estimator</a></code></td><td>An <code>Estimator</code> that can be used in a <a href="fit.html">Fit</a> operation.</td></tr>
</tbody>
</table>


## Parameters


<table class="table">
<thead>
<tr>
<th style="width:15%">Name</th>
<th style="width:15%">Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>

<tr>
<td><code>checkpoint interval</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The checkpoint interval. E.g. 10 means that the cache will get checkpointed
every 10 iterations.</td>
</tr>

<tr>
<td><code>k</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The number of clusters to create.</td>
</tr>

<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum number of iterations.</td>
</tr>

<tr>
<td><code>optimizer</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>Optimizer or inference algorithm used to estimate the LDA model. Currently supported:
Online Variational Bayes, Expectation-Maximization. Possible values: <code>["online", "em"]</code></td>
</tr>

<tr>
<td><code>subsampling rate</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Fraction of the corpus to be sampled and used in each iteration of mini-batch gradient
descent. Note that this should be adjusted in synchronization with `max iterations` so the
entire corpus is used. Specifically, set both so that `max iterations` * `subsampling rate`
>= 1.
.</td>
</tr>

<tr>
<td><code>topic distribution column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>Output column with estimates of the topic mixture distribution for each document
(often called \"theta\" in the literature). Returns a vector of zeros for
an empty document.</td>
</tr>

<tr>
<td><code>features column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The features column for model fitting.</td>
</tr>

<tr>
<td><code>seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The random seed.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

