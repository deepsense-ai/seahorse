---
layout: global
displayTitle: Split
title: Split
description: Split
usesMathJax: true
includeOperationsMenu: true
---

Splits a [DataFrame](../classes/dataframe.html) into two separate `DataFrames`. Each row from the
input `DataFrame` will always end up in one of the result `DataFrames`, but never in both.

There are two `split modes`:

* `RANDOM` where rows are split randomly with specified `split ratio` and `seed`
* `CONDITIONAL` where rows are split into two `DataFrames` - satisfying an SQL `condition` and not satisfying it

The `Split` operation does not preserve row order.

**Since**: Seahorse 0.4.0

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
<td>The <code>DataFrame</code> to split.</td>
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
<td>The first part of the input <code>DataFrame</code>.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The second part of the input <code>DataFrame</code>.</td>
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
<td><code>split mode</code></td>
<td><code><a href="../parameter_types.html#single-choice">Single Choice</a></code></td>
<td>The split mode. Possible values are:
  <code>RANDOM</code>, <code>CONDITIONAL</code>.
</td>
</tr>
<tr>
<td><code>split ratio</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Valid only if <code>split mode = RANDOM</code>.
  A number between 0 and 1 describing how much of the input <code>DataFrame</code> will end up in
  the first part of the split. Example: <code>split ratio = 0.3</code> means that the first output
  <code>DataFrame</code> will contain about 30% of the rows of the input <code>DataFrame</code>,
  and the second output <code>DataFrame</code> will contain the rest (about 70%) of the rows
  of the input <code>DataFrame</code>.
</td>
</tr>
<tr>
<td><code>seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Valid only if <code>split mode = RANDOM</code>.
  An integer between -1073741824 and 1073741823 that is used as a seed for the random number
  generator. A fixed value of this parameter allows to produce repeatable results.
</td>
</tr>
<tr>
<td><code>condition</code></td>
<td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
<td>Valid only if <code>split mode = CONDITIONAL</code>.
The split condition. Rows satisfying given condition will be included into first output <code>DataFrame</code>
and rows not satifying it will be included into second output <code>DataFrame</code>.
It should be <code>Spark SQL</code> condition (as used in <code>WHERE</code> condition).</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/Split.md %}
