.. Copyright (c) 2015, CodiLime, Inc.

Ridge regression
================

==========
Descriptor
==========

Creates untrained ridge regression model.

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
     - UntrainedRidgeRegression
     - Untrained model


===========
Description
===========
Operation used to initialize ridge regression model (i.e. uses L2 regularization).
Regression is performed using stochastic gradient descent minimizing mean squared error:
:math:`\frac{1}{2}(w^Tx-y)^2 + \lambda\cdot\frac{1}{2}||w||^2_2`, where :math:`x`
is vector of features, :math:`y` is label, :math:`w` is vector of weights of model,
and :math:`\lambda` is regularization parameter.

------
Params
------

1. ``regularization: Numeric`` - regularization parameter used in regression.
    Should be non-negative.
