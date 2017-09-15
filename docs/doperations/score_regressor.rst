.. Copyright (c) 2015, CodiLime, Inc.

Score regressor
===============

==========
Descriptor
==========

Scores DataFrame using trained model.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----
.. list-table::
   :widths: 15 30 55
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - Regressor with Scorable
     - Predictive model
   * - 1
     - DataFrame
     - DataFrame to score

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
     - DataFrame
     - Scored DataFrame


===========
Description
===========
Operation used to score DataFrame using previously trained regressor.
It assumes that input DataFrame contains columns that were used to train model, and that they have
numeric type.
If not, ``ColumnsDoNotExistException`` or ``WrongColumnTypeException`` are thrown respectively.
Result column will have name of target column from training DataFrame,
with ``'_prediction'`` appended.
If this name is occupied, ``'_1'`` will be appended to it
(or ``'_2'``, ``'_3'`` etc. so that uniqueness of column names is preserved).
Result column will be appended to scored DataFrame.
