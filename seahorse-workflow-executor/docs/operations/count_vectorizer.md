---
layout: global
displayTitle: Count Vectorizer
title: Count Vectorizer
description: Count Vectorizer
usesMathJax: true
includeOperationsMenu: true
---
Extracts the vocabulary from a given collection of documents and generates a vector
of token counts for each document.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-features.html#countvectorizer">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.feature.CountVectorizer">org.apache.spark.ml.feature.CountVectorizer documentation</a>.

**Since**: Seahorse 1.0.0

## Input


<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Type Qualifier</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>The input <code>DataFrame</code>.</td></tr>
</tbody>
</table>


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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>The output <code>DataFrame</code>.</td></tr><tr><td><code>1</code></td><td><code><a href="../classes/transformer.html">Transformer</a></code></td><td>A <code>Transformer</code> that allows to apply the operation on other <code>DataFrames</code> using a <a href="transform.html">Transform</a>.</td></tr>
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
<td><code>input column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The input column name.</td>
</tr>

<tr>
<td><code>output</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>Output generation mode. Possible values: <code>["replace input column", "append new column"]</code></td>
</tr>

<tr>
<td><code>max vocabulary size</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum size of the vocabulary.</td>
</tr>

<tr>
<td><code>min different documents</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Specifies the minimum number of different documents a term must appear in to be included in the vocabulary.</td>
</tr>

<tr>
<td><code>min term frequency</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>A filter to ignore rare words in a document. For each document, terms with
a frequency/count less than the given threshold are ignored. If this is an integer >= 1,
then this specifies a count (of times the term must appear in the document); if this is
a double in [0,1), then it specifies a fraction (out of the document's token count).
Note that the parameter is only used in transform of CountVectorizer model and does not
affect fitting.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/CountVectorizer.md %}
