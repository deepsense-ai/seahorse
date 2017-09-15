.. Copyright (c) 2015, CodiLime, Inc.

Train regressor
===============

==========
Descriptor
==========

Trains regression model on DataFrame, returning trained model.

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
     - Trainable
     - Model to train
   * - 1
     - DataFrame
     - DataFrame to train model on

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
     - Scorable
     - Trained model


===========
Description
===========
Operation used to train regression model on DataFrame.
It assumes that input DataFrame contains columns that were used to train model, and that they have
numeric type.
If not, ``ColumnsDoNotExistException`` or ``WrongColumnTypeException`` are thrown respectively.
Result column will have name of target column from training DataFrame,
with ``'_prediction'`` appended.
If this name is occupied, ``'_1'`` will be appended to it
(or ``'_2'``, ``'_3'`` etc. so that uniqueness of column names is preserved).
Result column will be appended to scored DataFrame.

------
Params
------

1. ``feature columns: ColumnSelector`` - subset of the DataFrame columns, used to train model.
   If columns selected by user have type different then Numeric, ``WrongColumnTypeException``
   will be thrown. If some of selected columns do not exist,
   ``ColumnDoesNotExistException`` will be thrown.
2. ``target column: SingleColumnSelector`` - column containing target of training
   (i. e. label to predict). If selected column has type different then ``Numeric``,
   ``WrongColumnTypeException`` will be thrown. If selected column does not exist,
   ``ColumnDoesNotExistException`` will be thrown.
