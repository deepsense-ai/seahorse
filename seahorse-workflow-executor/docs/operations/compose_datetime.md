---
layout: global
displayTitle: Compose Datetime
title: Compose Datetime
description: Compose Datetime
usesMathJax: true
includeOperationsMenu: true
---

Combines parts of a timestamp from existing columns into a new timestamp column.
For example: when combining the columns ``bith_year``, ``birth_month`` and ``birth_date``, a new column containing birth date of type `Timestamp` will be created.



Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another [DataFrame](../classes/dataframe.html) with a [Transform](transform.html) operation.

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
<td>The <code>DataFrame</code> with the timestamp part columns to compose.</td>
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
<td>The <code>DataFrame</code> with new column with timestamp composed from the original timestamp part columns.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>transformer</code> that allows to apply the operation on another DataFrame using a
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
<td><code>parts</code></td>
<td><code><a href="../parameter_types.html#multiple-choice">MultipleChoice</a></code></td>
<td>Columns containing parts of the timestamp to be composed.
  Every choice has a nested <code>SingleColumnSelector</code>.
  Possible values are pairs of timestamp part: <code> [year, month, day, hour, minutes, seconds] </code> and the selection of the column containing that part.
  If the column selected by the user has a type other than <code>Numeric</code>,
  <code>WrongColumnTypeException</code> will be thrown.
  If the selected column does not exist, a <code>ColumnDoesNotExistException</code> will be thrown.</td>
</tr>
<tr>
<td><code>output column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>Name of the result column.</td>
</tr>
</tbody>
</table>

[comment]: # (Example can't be generated:)
[comment]: # (MultipleChoice can't be handled by ParametersHtmlFormatter)

## Example

### Parameters

<table class="table">
<thead>
<tr>
  <th style="width:20%">Name</th>
  <th style="width:80%">Value</th>
</tr>
</thead>
<tbody>

<tr>
<td><code>parts</code></td>
<td>[(year, "year"), (month, "month"), (day, "day"), (hour, "hour")]</td>
</tr>

<tr>
<td><code>output column</code></td>
<td>"birthdate"</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>name</th>
      <th>year</th>
      <th>month</th>
      <th>day</th>
      <th>hour</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Ashley</td>
      <td>1978.0</td>
      <td>3.0</td>
      <td>2.0</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>Tali</td>
      <td>1982.0</td>
      <td>1.0</td>
      <td>2.0</td>
      <td>3.0</td>
    </tr>
    <tr>
      <td>Liara</td>
      <td>1902.0</td>
      <td>11.0</td>
      <td>20.0</td>
      <td>5.0</td>
    </tr>
    <tr>
      <td>Miranda</td>
      <td>1993.0</td>
      <td>5.0</td>
      <td>1.0</td>
      <td>17.0</td>
    </tr>
    <tr>
      <td>Samara</td>
      <td>1921.0</td>
      <td>9.0</td>
      <td>29.0</td>
      <td>22.0</td>
    </tr>
    <tr>
      <td>Jack</td>
      <td>1990.0</td>
      <td>4.0</td>
      <td>2.0</td>
      <td>7.0</td>
    </tr>
    <tr>
      <td>Edi</td>
      <td>2006.0</td>
      <td>8.0</td>
      <td>4.0</td>
      <td>20.0</td>
    </tr>
    <tr>
      <td>Kasumi</td>
      <td>1962.0</td>
      <td>4.0</td>
      <td>17.0</td>
      <td>16.0</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>name</th>
      <th>year</th>
      <th>month</th>
      <th>day</th>
      <th>hour</th>
      <th>birthdate</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Ashley</td>
      <td>1978.0</td>
      <td>3.0</td>
      <td>2.0</td>
      <td>1.0</td>
      <td>1978-03-02 01:00:00.0</td>
    </tr>
    <tr>
      <td>Tali</td>
      <td>1982.0</td>
      <td>1.0</td>
      <td>2.0</td>
      <td>3.0</td>
      <td>1982-01-02 03:00:00.0</td>
    </tr>
    <tr>
      <td>Liara</td>
      <td>1902.0</td>
      <td>11.0</td>
      <td>20.0</td>
      <td>5.0</td>
      <td>1902-11-20 05:00:00.0</td>
    </tr>
    <tr>
      <td>Miranda</td>
      <td>1993.0</td>
      <td>5.0</td>
      <td>1.0</td>
      <td>17.0</td>
      <td>1993-05-01 17:00:00.0</td>
    </tr>
    <tr>
      <td>Samara</td>
      <td>1921.0</td>
      <td>9.0</td>
      <td>29.0</td>
      <td>22.0</td>
      <td>1921-09-29 22:00:00.0</td>
    </tr>
    <tr>
      <td>Jack</td>
      <td>1990.0</td>
      <td>4.0</td>
      <td>2.0</td>
      <td>7.0</td>
      <td>1990-04-02 07:00:00.0</td>
    </tr>
    <tr>
      <td>Edi</td>
      <td>2006.0</td>
      <td>8.0</td>
      <td>4.0</td>
      <td>20.0</td>
      <td>2006-08-04 20:00:00.0</td>
    </tr>
    <tr>
      <td>Kasumi</td>
      <td>1962.0</td>
      <td>4.0</td>
      <td>17.0</td>
      <td>16.0</td>
      <td>1962-04-17 16:00:00.0</td>
    </tr>
  </tbody>
</table>
