.. Copyright (c) 2015, CodiLime, Inc.

Score classifier
================

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
     - Scorable with Classifier
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
An operation used to score a DataFrame using a previously trained classifier.
It assumes that the input DataFrame contains columns that were used to train the model,
and that they have a numeric type.
If not, ``ColumnsDoNotExistException`` or ``WrongColumnTypeException`` are thrown respectively.
The result column will have a name of the target column from the training DataFrame,
with ``'_prediction'`` appended.
If the name is occupied, ``'_1'`` will be appended to it
(or ``'_2'``, ``'_3'`` etc. so that uniqueness of column names is preserved).
The result column will be appended to the scored DataFrame.
