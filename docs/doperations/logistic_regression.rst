.. Copyright (c) 2015, CodiLime, Inc.

Logistic regression
===================

==========
Descriptor
==========

Creates untrained logistic regression model.

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
     - UntrainedLogisticRegression
     - Untrained model


===========
Description
===========
Operation used to initialize binary logistic regression model.
Regression is performed using LBFGS minimizing following loss function:

:math:`\log(1 + \exp(-yw^Tx))`

where :math:`x`
is vector of features, :math:`y` is label (-1 or 1) and :math:`w` is vector of weights of model.

------
Params
------

1. ``iterationsNumber: Numeric`` - Max number of iterations for algorithm to perform.
2. ``tolerance: Numeric`` - The convergence tolerance of iterations for LBFGS.
   Smaller value will lead to higher accuracy with the cost of more iterations.
