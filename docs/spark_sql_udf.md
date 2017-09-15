---
layout: global
displayTitle: Spark SQL User Defined Functions
menuTab: reference
title: Spark SQL User Defined Functions
description: Spark SQL User Defined Functions
usesMathJax: true
---

Seahorse uses
<a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#sql">Spark SQL</a>
as syntax for conditions, formulas and expressions in a few operations.
`Spark SQL` allows execution of simple SQL or HiveQL queries.

This syntax has been enriched by registering User Defined Functions for usage in queries.

- Mathematical functions

  - ``ABS(expr: Double)`` returns the absolute (positive) value of the specified numeric expression

  - ``EXP(expr: Double)`` returns
  <a target="_blank" href="https://en.wikipedia.org/wiki/E_(mathematical_constant)">$$ e $$</a>
  to the power of the specified expression

  - ``POW(expr: Double, pow: Double)`` returns the value of the specified expression to the
  specified power

  - ``SQRT(expr: Double)`` returns the square root of the specified float value.

  - ``SIN(expr: Double)`` returns the trigonometric sine of the specified angle in radians

  - ``COS(expr: Double)`` returns the trigonometric cosine of the specified angle in radians

  - ``TAN(expr: Double)`` returns the trigonometric tangent of the specified angle in radians

  - ``LN(expr: Double)`` returns the natural logarithm of the expression

  - ``MINIMUM(expr1: Double, expr2: Double)`` returns minimum of the given expressions

  - ``MAXIMUM(expr1: Double, expr2: Double)`` returns maximum of the given expressions

  - ``CEIL(expr: Double)`` returns the smallest integer greater than or equal to
  the specified numeric expression

  - ``FLOOR(expr: Double)`` returns the largest integer less than or equal to the
  numeric expression

  - ``SIGNUM(expr: Double)`` returns zero if the argument is zero, 1.0 if the argument is greater
  than zero, -1.0 if the argument is less than zero

<br/>
**NOTE:** The parser is case sensitive - function names have to be written in uppercase,
column names parsing is also case sensitive.<br/>
**NOTE:** `null` values are propagated, e.g. the expression ``POW(SIN(score), 2.0) + 1.0``
will return `null` for rows containing `null` values in ``score`` column.<br/>

