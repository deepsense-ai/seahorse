.. Copyright (c) 2015, CodiLime, Inc.

Apply Transformation
====================

==========
Descriptor
==========

Operation applies transformation to the given DataFrame.

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
     - Transformation
     - Transformation to apply.
   * - 1
     - DataFrame
     - DataFrame to transform.

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
     - DataFrame which is a result of applying transformation on the input DataFrame.


===========
Description
===========
ApplyTransformation is used to transform DataFrame using some transformation.

-----
Input
-----
1. Transformation to apply.
2. DataFrame to transform.

------
Output
------

1. Transformed DataFrame.

------
Params
------

  None


