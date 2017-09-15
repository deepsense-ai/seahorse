---
layout: global
displayTitle: Python Notebook
title: Python Notebook
description: Python Notebook
includeOperationsMenu: true
---

The `Python Notebook` operation combines the capabilities of the
<a target="_blank" href="{{ site.SPARK_DOCS }}/api/python/">PySpark</a> shell with
the rich selection of features the <a target="_blank" href="https://jupyter.org/">Jupyter
notebook</a> offers. It provides users with a unique environment to explore their data sets.

`Python Notebooks` allow the user to analyze their data by operating directly on the input `DataFrame`
by means of Apache Spark Python API. The results of Python code execution are presented immediately
and retained across user's sessions. Due to their versatility, `Python Notebooks` serve both as a way to
get familiarized with the data and as a record of completed research.

In order to use the `Python Notebook`, the user has to drag and drop the operation onto the canvas and
connect a [DataFrame](../classes/dataframe.html) to its input port. The connected `DataFrame` can
be accessed from within the `Python Notebook` by calling the `dataframe()` function.

The user can start editing the code by clicking **Open notebook** in the `Python Notebook` operation's
parameters panel.

{% markdown operations/python_global_scope.md %}

Data visualization can be achieved using the
<a target="_blank" href="{{ site.PANDAS_LIBRARY_ADDRESS }}">pandas</a> library.

**Remark**: In case of big `DataFrames`, the user should sample the DataFrame before using pandas.

**Since**: Seahorse 1.0.0

#### Examples

{% highlight python %}
dataframe().show()
{% endhighlight %}

    +--------------------+--------+--------+---------+----+----+------+
    |            datetime|weather2|weather3|windspeed| hum|temp| atemp|
    +--------------------+--------+--------+---------+----+----+------+
    |2011-01-01T00:00:...|       0|       0|        0|0.81|0.24|0.2879|
    |2011-01-01T01:00:...|       0|       0|        0| 0.8|0.22|0.2727|
    |2011-01-01T02:00:...|       0|       0|        0| 0.8|0.22|0.2727|
    |2011-01-01T03:00:...|       0|       0|        0|0.75|0.24|0.2879|
    |2011-01-01T04:00:...|       0|       0|        0|0.75|0.24|0.2879|
    |2011-01-01T05:00:...|       1|       0|   0.0896|0.75|0.24|0.2576|
    |2011-01-01T06:00:...|       0|       0|        0| 0.8|0.22|0.2727|
    |2011-01-01T07:00:...|       0|       0|        0|0.86| 0.2|0.2576|
    |2011-01-01T08:00:...|       0|       0|        0|0.75|0.24|0.2879|
    |2011-01-01T09:00:...|       0|       0|        0|0.76|0.32|0.3485|
    |2011-01-01T10:00:...|       0|       0|   0.2537|0.76|0.38|0.3939|
    |2011-01-01T11:00:...|       0|       0|   0.2836|0.81|0.36|0.3333|
    |2011-01-01T12:00:...|       0|       0|   0.2836|0.77|0.42|0.4242|
    |2011-01-01T13:00:...|       1|       0|   0.2985|0.72|0.46|0.4545|
    |2011-01-01T14:00:...|       1|       0|   0.2836|0.72|0.46|0.4545|
    |2011-01-01T15:00:...|       1|       0|   0.2985|0.77|0.44|0.4394|
    |2011-01-01T16:00:...|       1|       0|   0.2985|0.82|0.42|0.4242|
    |2011-01-01T17:00:...|       1|       0|   0.2836|0.82|0.44|0.4394|
    |2011-01-01T18:00:...|       0|       1|   0.2537|0.88|0.42|0.4242|
    |2011-01-01T19:00:...|       0|       1|   0.2537|0.88|0.42|0.4242|
    +--------------------+--------+--------+---------+----+----+------+

{% highlight python %}
import matplotlib.pyplot as plt
%matplotlib inline
df = dataframe().toPandas()
df['atemp'].plot()
{% endhighlight %}
<img class="img-responsive" src="../img/notebook_plot.png" />

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
<td>The input <code>DataFrame</code> that can be accessed by calling <code>dataframe()</code>.</td>
</tr>
</tbody>
</table>

## Output

The `Python Notebook` operation does not produce any output.

## Parameters


<table class="table">
  <thead>
    <tr>
      <th style="width:22%">Name</th>
      <th style="width:23%">Type</th>
      <th style="width:55%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <code>execute notebook</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>
        If <code>true</code> then notebook code will be run on notebook
        node execution. In batch mode notebook will never be executed,
        no matter what parameter value will be.
      </td>
    </tr>

    <tr>
      <td>
        <code>send e-mail report</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>Appears only if <code>execute notebook = true</code>.
        If <code>true</code> then notebook result will be sent via e-mail.
        Field with e-mail address must be filled.
      </td>
    </tr>

    <tr>
      <td>
        <code>e-mail address</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td> Appears only if <code>send e-mail report = true</code>.
      E-mail address where html with notebook execution result
      should be sent to.
      </td>
    </tr>

  </tbody>
</table>
