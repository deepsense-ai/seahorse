.. Copyright (c) 2015, CodiLime, Inc.

Write DataFrame
===============

==========
Descriptor
==========

Writes DataFrame to the Deepsense.io storage.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----

.. list-table:: Input
   :widths: 15 20 65
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - DataFrame
     - DataFrame to write in the DeepSense.io storage.

------
Output
------
None


===========
Description
===========
Operation to write a DataFrame in the Deepsense.io storage.
Written DataFrame can be later retrieved using :doc:`read_data_frame` operation.

-----
Input
-----
1. DataFrame to write.

------
Output
------
None

------
Params
------
1. ``name: String`` - user friendly name for the DataFrame to easily identify it
   among others.
2. ``description: String`` - description for the DataFrame.
