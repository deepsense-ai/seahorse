.. Copyright (c) 2015, CodiLime, Inc.

Evaluate Classification
=======================

==========
Descriptor
==========

Operation evaluates classification predictions provided in the DataFrame.

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
     - Report providing metrics about classification predictions.


===========
Description
===========
EvaluateRegression operation provides metrics evaluating classification predictions quality
in the given DataFrame.

-----
Input
-----
1. DataFrame with predictions in **predictionColumn** and **targetColumn** containing expected
   values.

------
Output
------
1. Report containing following metrics:

  * Accuracy
  * Precision
  * Recall
  * Confusion matrix
  * ROC curve
  * AUC

------
Params
------

1. ``targetColumn: SingleColumnSelection`` - Column with expected values.
   Column has to have numeric type.
2. ``predictionColumn: SingleColumnSelection`` - Column with classification prediction.
   Column has to have numeric type.

=======
Example
=======

----------------------
Report content example
----------------------

.. code-block:: json

   {
     "name": "Evaluate Classification",
     "tables": {
       "summary": {
         "name": "summary",
         "description": "Evaluate classification summary",
         "columnNames": ["DataFrame size", "AUC", "Logarithmic Loss"],
         "blockType": "table",
         "rowNames": null,
         "values": [["24", "1", "0.01005"]]
       },
       "accuracy": {
         "name": "accuracy",
         "description": "Accuracy",
         "columnNames": ["Threshold", "Accuracy"],
         "blockType": "table",
         "rowNames": null,
         "values": [["0.99", "1"], ["0.01", "0.5"]]
       },
       "fMeasureByThreshold": {
         "name": "fMeasureByThreshold",
         "description": "F-Measure (F1 score) by threshold",
         "columnNames": ["Threshold", "F-Measure"],
         "blockType": "table",
         "rowNames": null,
         "values": [["0.99", "1"], ["0.01", "0.666667"]]
       },
       "roc": {
         "name": "roc",
         "description": "Receiver Operating Characteristic curve",
         "columnNames": ["False positive rate", "True positive rate"],
         "blockType": "table",
         "rowNames": null,
         "values": [["0", "0"], ["0", "1"], ["1", "1"], ["1", "1"]]
       }
     },
     "distributions": {
     }
   }
