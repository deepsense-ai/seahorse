---
layout: global
displayTitle: R Evaluator
title: R Evaluator
description: R Evaluator
usesMathJax: true
includeOperationsMenu: true
---
Creates an R Evaluator that executes an R function provided by the user on a [DataFrame](../classes/dataframe.html).
Returns the result metric of the execution as a [MetricValue](../classes/metric_value.html).

The R function that will be executed must:

* be named <code>evaluate</code>,

* take exactly one argument of type `DataFrame`,

* return a `numeric` value.

This operation has an `is larger better` that indicates whether the returned metric is better to be maximized or minimized.
It is especially useful in [Grid Search](../operations/grid_search.html) operation that searches for the best [Estimator](../classes/estimator.html) using a given metric.

#### Example R code:
{% highlight R %}
evaluate <- function(dataframe){
  n <- nrow(dataframe)
  sq.error.column <- (dataframe$label - dataframe$prediction) ^ 2
  sq.error.sum.column <- sum(sq.error.column)
  sq.error.sum <- as.data.frame(agg(dataframe, sq.error.sum.column))
  rmse <- sqrt(sq.error.sum / n)
  return(rmse)
}
{% endhighlight %}

**Remark**: Trying to install an R package for the first time in multiple operations in parallel might cause erorrs. The operation should work properly when run again.

**Since**: Seahorse 1.3.0

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
    <tr><td><code>0</code></td><td><code><a href="../classes/evaluator.html">Evaluator</a></code></td><td>An <code>Evaluator</code> that can be used in an <a href="evaluate.html">Evaluate</a> operation.</td></tr>
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
<td><code>metric name</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>Name of the metric.</td>
</tr>
<tr>
<td><code>R evaluator code</code></td>
<td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
<td>The R code to be executed. It has to contain an R function complying to signature
presented in the operation's description.</td>
</tr>
<tr>
<td><code>is larger better</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Indicates whether the returned metric is better to be maximized or minimized.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>
