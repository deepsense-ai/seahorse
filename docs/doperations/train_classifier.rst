.. Copyright (c) 2015, CodiLime, Inc.

Train classifier
================

==========
Descriptor
==========

Trains classifier model on DataFrame, returning trained model.

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
     - Classifier with Trainable
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
     - Classifier with Scorable
     - Trained model


===========
Description
===========
An operation used to train a classifier model on a DataFrame.

------
Params
------

1. ``feature columns: ColumnSelector`` - subset of the DataFrame columns, used to train model.
   If columns selected by user have type different than Numeric, ``WrongColumnTypeException``
   will be thrown. If some of selected columns do not exist,
   ``ColumnDoesNotExistException`` will be thrown.
2. ``target column: SingleColumnSelector`` - column containing target of training
   (i. e. label to predict). If selected column has type different than ``Numeric``,
   ``WrongColumnTypeException`` will be thrown. If selected column does not exist,
   ``ColumnDoesNotExistException`` will be thrown.

While training, it expects all values in target column to be either 0 or 1.
.. TODO What if this requirement is not met?
