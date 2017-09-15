.. Copyright (c) 2015, CodiLime, Inc.

Mathematical operation
===============

==========
Descriptor
==========

Mathematical operation creates a Transformation that creates a new column based on a mathematical formula.
The Transformation can be applied to a DataFrame with ``Apply Transformation`` operation.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----

None

------
Output
------
.. list-table::
  :widths: 15 30 55
  :header-rows: 1

  * - Port
    - Data Type
    - Description
  * - 0
    - Transformation
    - Transformation containing the formula

===========
Description
===========
Creates a Transformation that adds a new column basing on the mathematical formula provided by the user.
The formula grammar is based on SQL expressions. Sample formulas:

- ``MAX(someColumn1, someColumn2) as col_max``

- ``POW(SIN(score), 2.0) + 1.0 as score_transformed``

- ``MIN(age, 5.0) as age_cutoff``

The name of the new column can be provide with the ``as`` keyword like in the examples above.
If the name is not provided the whole formula will be used as a new column name.
The formula parser is case sensitive - function names have to be written in uppercase,
column names parsing is also case sensitive. I.e. SCORE is different than score.

Please make sure that the formula is always producing Double values i.e.
``MIN(weight, 2)`` will produce Integer value in some cases,
it should be changed to ``MIN(weight, 2.0)``

Null values are propagated. The expression ``POW(SIN(score), 2.0) + 1.0 as score_transformed``
will return null for rows containing null values in score column

Available functions and operators:

- All the basic operators: ``+``, ``-``, ``/``, ``*``

- Mathematical functions

  - ``ABS(expr: Double)`` returns the absolute (positive) value of the specified numeric expression

  - ``EXP(expr: Double)`` returns e to the power of the specified expression

  - ``POWER(expr: Double, pow: Double)`` returns the value of the specified expression to the specified power

  - ``SQRT(expr: Double)`` return the square root of the specified float value.

  - ``SIN(expr: Double)`` returns the trigonometric sine of the specified angle, in radians

  - ``COS(expr: Double)`` returns the trigonometric cosine of the specified angle, in radians

  - ``TAN(expr: Double)`` returns the trigonometric tangent of the specified angle, in radians

  - ``LN(expr: Double)`` returns the natural logarithm of the expression

  - ``MIN(expr1: Double, expr2: Double)`` returns minimum of the given expressions

  - ``MAX(expr1: Double, expr2: Double)`` returns maximum of the given expressions

------
Params
------

1. ``formula: String`` - mathematical formula

=======
Example
=======

----------
Input Data
----------

1. formula = "MIN(Weight, 10.0) as WeightCutoff"

-----------
Output Data
-----------

1. Transformation containing the formula

-----------------------------------------------------
Applying transformation with ``Apply Transformation``
-----------------------------------------------------

Assuming that the above Transformation is applied to the following DataFrame

========= ======= ======
Animal    Kind    Weight
========= ======= ======
Cow       Mammal  300.0
Ostrich   Bird    0.5
Dog       Mammal  5.0
Sparrow   Bird    0.5
Thing     null    null
========= ======= ======

-----------
Output Data
-----------

========= ======= ====== =======
Animal    Kind    Weight WeightCutoff
========= ======= ====== =======
Cow       Mammal  300.0  10.0
Ostrich   Bird    0.5    0.5
Dog       Mammal  5.0    5.0
Sparrow   Bird    0.5    0.5
Thing     null    null   null
========= ======= ====== =======
