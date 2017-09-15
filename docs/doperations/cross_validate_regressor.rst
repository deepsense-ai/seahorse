.. Copyright (c) 2015, CodiLime, Inc.

Cross-validate Regressor
========================

==========
Descriptor
==========

Cross-validates regressor,
produces regressor trained on entire DataFrame and a cross-validation report.

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
     - Regressor with Trainable
     - Regressor to train and cross-validate.
   * - 1
     - DataFrame
     - DataFrame to train and cross-validate on.

------
Output
------
.. list-table:: Input
   :widths: 15 30 55
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - Regressor with Scorable
     - Regressor trained on entire DateFrame.
   * - 1
     - Report
     - Report of cross-validation.


===========
Description
===========
Operation to train regressor on given DataFrame.
Additionally performs cross-validation and produces report of cross-validation.

------
Params
------
1. ``numOfFolds: Numeric`` - Number of folds (**default: 10**).
   If number of folds is greater than number of rows in training DataFrame, greatest applicable
   number of folds is used.
   Number of rows equal 0 results in omitting generation of cross-validation report
   (empty report is returned).
2. ``shuffle: Choice`` - Indicate if shuffle on DataFrame should be performed before
   cross-validation.

   - ``NO`` - Shuffle will not be performed.
   - ``YES`` - Shuffle will be performed.
      - ``seed: Numeric`` - Seed value for shuffle (**default: 0**).
3. ``feature columns: ColumnSelector`` - Subset of the DataFrame columns, used to train model.
   If columns selected by user have type different then Numeric, ``WrongColumnTypeException``
   will be thrown. If some of selected columns do not exist,
   ``ColumnDoesNotExistException`` will be thrown.
4. ``target column: SingleColumnSelector`` - Column containing target of training
   (i. e. label to predict). If selected column has type different then ``Numeric``,
   ``WrongColumnTypeException`` will be thrown. If selected column does not exist,
   ``ColumnDoesNotExistException`` will be thrown.

=======
Example
=======

----------------
Operation Params
----------------
1. numOfFolds = 3
2. seed = 0

----------------------
Report content example
----------------------

{

  "name": "Cross-validate Regressor Report",

  "tables": {

    "Cross-validate report table": {

      "name": "Cross-validate report table",

      "description": "",

      "columnNames": ["foldNumber", "trainSetSize", "testSetSize",

        "explainedVariance", "meanAbsoluteError",

        "meanSquaredError", "r2", "rootMeanSquaredError"],

      "blockType": "table",

      "rowNames": ["1", "2", "3", "average"],

      "values": [
        ["1", "17", "7", "1.0", "2.854859206178974E-16", "2.887794385184061E-31", "1.0", "5.373820228835405E-16"],

        ["2", "13", "11", "1.0", "9.285501660501309E-16", "1.0936480731473119E-30", "1.0", "1.045776301676086E-15"],

        ["3", "18", "6", "1.0", "9.62193288008469E-16", "1.150422153447309E-30", "1.0", "1.0725773414758066E-15"],

        ["average", "", "", "1.0", "7.254097915588324E-16", "8.442832217043423E-31", "1.0", "8.85245222011811E-16"]

      ]

    }

  }

}

