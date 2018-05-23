---
layout: global
displayTitle: R Transformation
title: R Transformation
description: R Transformation
usesMathJax: true
includeOperationsMenu: true
---

Executes an R function provided by the user on a [DataFrame](../classes/dataframe.html) connected to its input port.
Returns the results of the execution as a `DataFrame`.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another `DataFrame` with a [Transform](transform.html) operation.

The R function that will be executed must:

* be named <code>transform</code>,

* take exactly one argument of type `DataFrame`,

* return a SparkR `SparkDataFrame`, an R `data.frame`, or data that can be converted to R `data.frame` using `data.frame()` function (single value, vector etc).

{% markdown operations/r_global_scope.md %}

#### Example R code:
{% highlight r %}
transform <- function(dataframe) {
  d1 <- dataframe[, c(2,3,4)]
  c2 <- collect(d1)
  fun <- function(x){ return(-x) }
  d3 <- lapply(c2, fun)
  d4 <- createDataFrame(as.data.frame(d3))
  return(d4)
}
{% endhighlight %}

**Remark**: Trying to install an R package for the first time in multiple operations in parallel might cause erorrs. The operation should work properly when run again.

**Since**: Seahorse 1.3.0

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
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The <code>DataFrame</code> that will be passed to the <code>transform</code> function.</td>
</tr>
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
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The return value of the <code>transform</code> function.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td><code>Transformer</code> that allows to apply the operation on another <code>DataFrames</code> using
<a href="transform.html">Transform</a>.</td>
</tr>
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
<td><code>code</code></td>
<td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
<td>The R code to be executed. It has to contain an R function complying to signature
presented in the operation's description.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/RTransformationMock.md %}
