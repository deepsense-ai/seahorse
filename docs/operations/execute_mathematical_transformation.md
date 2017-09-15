---
layout: documentation
displayTitle: Execute Mathematical Transformation
title: Execute Mathematical Transformation
description: Execute Mathematical Transformation
usesMathJax: true
includeOperationsMenu: true
---

Applies a mathematical formula to a [DataFrame](../classes/dataframe.html) and produces
a [Transformer](../classes/transformer.html) that represents the mathematical formula.
Later on, the `Transformer` can be applied to a [DataFrame](../classes/dataframe.html)
with a [Transform](transform.html) operation.


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
<td>The input <code>DataFrame</code>.</td>
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
<td><code>0</code></td><td>
<code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The results of the transformation.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>transformer</code> that allows to apply the operation on another <code>DataFrame</code> using
<code><a href="transform.html">Transform</a></code>.</td>
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
    <td><code>input column</code></td>
    <td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
    <td>The input column that can be accessed with <code>input column alias</code>.</td>
  </tr>
  <tr>
    <td><code>input column alias</code></td>
    <td><code><a href="../parameter_types.html#string">String</a></code></td>
    <td>The identifier that can be used in the SQL formula to refer the input column.</td>
  </tr>
  <tr>
    <td><code>formula</code></td>
    <td><code><a href="../parameter_types.html#string">String</a></code></td>
    <td>The mathematical formula. The formula grammar is based on SQL expressions (see below).</td>
  </tr>
  <tr>
    <td><code>output column name</code></td>
    <td><code><a href="../parameter_types.html#string">String</a></code></td>
    <td>The name of the newly created column holding the result.</td>
  </tr>
</tbody>
</table>

## Formula Grammar

The formula is a Spark SQL expression. Sample formulas:

- ``MAXIMUM(someColumn1, someColumn2)``

- ``POW(SIN(score), 2.0) + 1.0``

- ``MINIMUM(age, 5.0)``

The name of the new column can be provided in ``output column name`` parameter.

The formula parser is case sensitive - function names have to be written in uppercase,
column names parsing is also case sensitive.

Only one column can be created - an expression ``SIN(someColumn), COS(someColumn)``
will not be parsed properly.

``null`` values are propagated. The expression ``POW(SIN(score), 2.0) + 1.0``
will return null for rows containing null values in ``score`` column.

Available functions and operators:

- All the basic operators: ``+``, ``-``, ``/``, ``*``

- Mathematical functions

  - ``ABS(expr: Double)`` returns the absolute (positive) value of the specified numeric expression

  - ``EXP(expr: Double)`` returns e to the power of the specified expression

  - ``POW(expr: Double, pow: Double)`` returns the value of the specified expression to the
  specified power

  - ``SQRT(expr: Double)`` returns the square root of the specified float value.

  - ``SIN(expr: Double)`` returns the trigonometric sine of the specified angle in radians

  - ``COS(expr: Double)`` returns the trigonometric cosine of the specified angle in radians

  - ``TAN(expr: Double)`` returns the trigonometric tangent of the specified angle in radians

  - ``LN(expr: Double)`` returns the natural logarithm of the expression

  - ``MINIMUM(expr1: Double, expr2: Double)`` returns minimum of the given expressions

  - ``MAXIMUM(expr1: Double, expr2: Double)`` returns maximum of the given expressions

  - ``CEIL(expr1: Double)`` returns the smallest integer greater than or equal to
  the specified numeric expression

  - ``FLOOR(expr1: Double)`` returns the largest integer less than or equal to the
  numeric expression

  - ``SIGNUM(expr: Double)`` returns zero if the argument is zero, 1.0 if the argument is greater
  than zero, -1.0 if the argument is less than zero

{% markdown operations/examples/ExecuteMathematicalTransformation.md %}
