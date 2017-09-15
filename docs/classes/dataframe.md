---
layout: documentation
displayTitle: DataFrame
title: DataFrame
includeClassesMenu: true

description: Seahorse documentation homepage
---

## Introduction

**DataFrame** is a distributed collection of data organized into named columns
backed by
<a target="_blank" href="https://spark.apache.org/docs/latest/sql-programming-guide.html#dataframes">Spark SQL’s DataFrame</a>.

Reusing Spark’s DataFrame data structure allows Seahorse to construct DataFrames
 from a wide array of sources, e.g. structured data files, tables in Hive or
external databases.

DataFrames are described by schema - a collection of column names and their
 types.

DataFrames support the following data types:

* numeric (double in Spark SQL)
* string
* boolean
* timestamp
* categorical

One of the main differences between Seahorse's implementation of DataFrame and
 Spark is categorical data type.

## Categorical Data Type

**Categorical data type** is a data type that is not directly available in Spark
 SQL. It is an enumerated type where data in a column is "indexed" by a discrete
 series of integers. The source values are mapped to Spark’s integers and
 additional metadata.

Using categoricals helps improving execution time of Machine Learning algorithms
 later on in workflows by using integers for non-integer values.

Quoting Wikipedia’s
<a target="_blank" href="https://en.wikipedia.org/wiki/Categorical_variable">Categorical variable</a>:

> In statistics, **a categorical variable** is a variable that can take on one
> of a limited, and usually fixed, number of possible values, thus assigning
> each individual to a particular group or "category."
