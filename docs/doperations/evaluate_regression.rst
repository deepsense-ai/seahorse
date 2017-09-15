.. Copyright (c) 2015, CodiLime, Inc.

Evaluate Regression
====================

==========
Descriptor
==========

Operation evaluates predictions provided in the DataFrame.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----

.. list-table:: Input
   :widths: 15 30 55
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - DataFrame
     - DataFrame to evaluate.

------
Output
------

.. list-table:: Output
   :widths: 15 30 55
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - Report
     - Report providing metrics about predictions.


===========
Description
===========
EvaluateRegression operation provides metrics evaluating predictions quality in the given DataFrame.

-----
Input
-----
1. DataFrame with predictions in **predictionColumn** and **targetColumn** containing expected
   values.

------
Output
------
1. Report containing following metrics: DataFrame Size, Explained Variance, Mean Absolute Error,
   Mean Square Error, r2, Root Mean Squared Error.

------
Params
------

1. ``targetColumn: SingleColumnSelection`` - Column with expected values.
   Column has to have numeric type.
2. ``predictionColumn: SingleColumnSelection`` - Column with prediction.
   Column has to have numeric type.
