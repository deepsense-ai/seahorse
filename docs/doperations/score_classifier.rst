.. Copyright (c) 2015, CodiLime Inc.

Score Classifier
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
     - Classifier with Scorable
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
The result column will have a name given in the ``prediction column`` parameter.
The result column will be appended to the scored DataFrame.

------
Params
------

1. ``prediction column: SingleColumnCreatorParameter`` - name of the newly created column, which
   contains generated predictions.
