---
layout: documentation
displayTitle: Notebook
title: Notebook
description: Notebook
includeOperationsMenu: true
---

The `Notebook` operation combines the capabilities of the
<a target="_blank" href="{{ site.SPARK_DOCS }}/api/python/">PySpark</a> shell with
the rich selection of features the <a target="_blank" href="https://jupyter.org/">Jupyter
notebook</a> offers. It provides users with a unique environment to explore their data sets.

`Notebooks` allow the user to analyze their data by operating directly on the input `DataFrame`
by means of Apache Spark Python API. The results of Python code execution are presented immediately
and retained across user's sessions. Due to their versatility, `Notebooks` serve both as a way to
get familiarized with the data and as a record of completed research.

In order to use the `Notebook`, the user has to drag and drop the operation onto the canvas and
connect a [DataFrame](../classes/dataframe.html) to its input port. The connected `DataFrame` can
be accessed from within the `Notebook` by calling the `dataframe()` function.

The user can start editing the code by clicking **Open notebook** in the `Notebook` operation's
parameters panel.

The variables and functions available in the `Notebooks`' global scope:

* `dataframe()` - a function that returns the input `DataFrame` for this `Notebook`.
Everytime the input `DataFrame` changes, the `dataframe()` returns the updated `DataFrame`.

* `sc` - Spark Context

* `sqlContext` - SQL Context

Data visualization can be achieved using the
<a target="_blank" href="{{ site.PANDAS_LIBRARY_ADDRESS }}">pandas</a> library.

**Remark**: In case of big `DataFrames`, the user should sample the DataFrame before using pandas.

**Since**: Seahorse 1.0.0

####Examples

{% highlight python %}
from pyspark.sql.types import *
from pyspark.sql import SQLContext, Row

schemaString = "datetime,weather2,weather3,windspeed,hum,temp,atemp"
fields = [StructField(field_name, StringType(), True) for field_name in schemaString.split(",")]
schema = StructType(fields)

lines = sc.textFile("weather.csv")
split = lines.map(lambda s: s.split(","))
df = sqlContext.createDataFrame(split, schema)
df.show()
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

The `Notebook` operation does not produce any output.
