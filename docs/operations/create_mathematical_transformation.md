---
layout: documentation
displayTitle: Create Mathematical Transformation
title: Create Mathematical Transformation
description: Create Mathematical Transformation
usesMathJax: true
includeOperationsMenu: true
---

Produces a [Mathematical Transformation](../classes/mathematical_transformation.html).
The Transformation creates a new column basing on a mathematical formula.
The Transformation can be applied to a [DataFrame](../classes/dataframe.html)
with [Apply Transformation](apply_transformation.html)
operation.


**Since**: Seahorse 0.4.0

## Input

Create Mathematical Transformation does not take any input.

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
<td><code><a href="../classes/mathematical_transformation.html">Mathematical Transformation</a></code></td>
<td>A Mathematical Transformation containing the formula.</td>
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
<td><code>formula</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>A mathematicla formula. The formula grammar is based on SQL expressions (see below).</td>
</tr>
<tr>
<td><code>column name</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Name of the newly created column holding the result.</td>
</tr>
</tbody>
</table>

## Formula Grammar

The formula grammar is based on SQL expressions. Sample formulas:

- ``MAXIMUM(someColumn1, someColumn2)``

- ``POW(SIN(score), 2.0) + 1.0``

- ``MINIMUM(age, 5.0)``

The name of the new column can be provided in ``column name`` parameter.

The formula parser is case sensitive - function names have to be written in uppercase,
column names parsing is also case sensitive.
Comma separated expressions are not valid - ``SIN(someColumn), COS(someColumn)`` will not be parsed properly.

Please make sure that the formula is always producing Double values i.e.
``MINIMUM(weight, 2)`` will produce Integer value in some cases,
and it should be changed to ``MINIMUM(weight, 2.0)``

``null`` values are propagated. The expression ``POW(SIN(score), 2.0) + 1.0``
will return null for rows containing null values in ``score`` column

Available functions and operators:

- All the basic operators: ``+``, ``-``, ``/``, ``*``

- Mathematical functions

  - ``ABS(expr: Double)`` returns the absolute (positive) value of the specified numeric expression

  - ``EXP(expr: Double)`` returns e to the power of the specified expression

  - ``POW(expr: Double, pow: Double)`` returns the value of the specified expression to the specified power

  - ``SQRT(expr: Double)`` return the square root of the specified float value.

  - ``SIN(expr: Double)`` returns the trigonometric sine of the specified angle, in radians

  - ``COS(expr: Double)`` returns the trigonometric cosine of the specified angle, in radians

  - ``TAN(expr: Double)`` returns the trigonometric tangent of the specified angle, in radians

  - ``LN(expr: Double)`` returns the natural logarithm of the expression

  - ``MINIMUM(expr1: Double, expr2: Double)`` returns minimum of the given expressions

  - ``MAXIMUM(expr1: Double, expr2: Double)`` returns maximum of the given expressions

  - ``CEIL(expr1: Double, expr2: Double)`` returns the smallest integer greater than or equal to the specified numeric expression

  - ``FLOOR(expr1: Double, expr2: Double)`` returns the largest integer less than or equal to the numeric expression

  - ``SIGNUM(expr: Double)`` returns zero if the argument is zero, 1.0 if the argument is greater than zero, -1.0 if the argument is less than zero

