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
.. list-table:: Intput
   :widths: 15 30 55
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - Scorable
     - Regressor with Scorable
   * - 1
     - DataFrame
     - DataFrame to score

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
     - DataFrame
     - Scored DataFrame


===========
Description
===========
Operation can be used to score DataFrame using previously trained regressor.

-----
Input
-----
1. Regressor with Scorable
2. DataFrame to score

------
Output
------

1. Scored DataFrame

------
Params
------

  None


