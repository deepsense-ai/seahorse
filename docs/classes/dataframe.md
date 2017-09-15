---
layout: global
displayTitle: DataFrame
title: DataFrame
includeClassesMenu: true

description: Seahorse documentation homepage
---

A `DataFrame` is a distributed collection of data organized into named columns
backed by <a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#datasets-and-dataframes">Spark SQL’s DataFrame</a>.

The use of Spark’s `DataFrame` data structure allows Seahorse to construct `DataFrames`
from a wide array of sources, e.g. structured data files, tables in Hive or
external databases.

`DataFrames` are described by a schema - a collection of column names and their
 types.

`DataFrames` support the same data types as Spark SQL DataFrames, including:

* double
* integer
* long
* string
* boolean
* timestamp
* vector
* array
