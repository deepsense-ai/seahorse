.. Copyright (c) 2015, CodiLime, Inc.

Split
=====

==========
Descriptor
==========

Operation to split DataFrame D to two separate DataFrames A, B.
:math:`D=A \cup B` and :math:`A \cap B = \emptyset`.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----

.. list-table:: Input
   :widths: 15 20 65
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - DataFrame
     - DataFrame to split.

------
Output
------

.. list-table:: Output
   :widths: 15 20 65
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - DataFrame
     - Part of the input DataFrame chosen based on specified criteria.
   * - 1
     - DataFrame
     - Part of the input DataFrame chosen based on specified criteria.


===========
Description
===========
Split DataFrame operation splits input DataFrame to two separate DataFrames.
Each row from the original DataFrame
will always be in one of the result DataFrames, but never in both.
Split operation does not preserve rows order.

Split operation provides following splitting criteria:

1. Random percentage split.
   User specifies split proportion e.g. 30% and optionally seed,
   which means that 30% of the original DataFrame
   will be put in first of the result DataFrame
   and the rest 70% will be put in the second one.
2. Predicate split.
   User can specify predicate in a simple predicate language (:ref:`language_label`).
   Rows for which the predicate evaluates to true are put
   in one of the result DataFrames
   and rows that evaluates to false will be put in the other one.

-----
Input
-----
1. DataFrame to split.

------
Output
------
1. First part of the original DataFrame, split result.
2. Second part of the original DataFrame, split result.

------
Params
------
1. ``split ratio: Numeric`` - Number :math:`x \in [0, 1]`. x=0.3 means that the input DataFrame will
   be split in 30% and 70% proportions.
2. ``stratify: SingleColumnSelector`` - **optional** parameter, if selected distribution of values
   in the selected column will be preserved in the result DataFrames.

.. _language_label:

===========
Appendix A.
===========
