.. Copyright (c) 2015, CodiLime Inc.

Save DataFrame
==============

==========
Descriptor
==========

Saves DataFrame to the Deepsense.io storage.

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
     - DataFrame to save in the DeepSense.io storage.

------
Output
------
None


===========
Description
===========
Operation to save a DataFrame in the Deepsense.io storage.
Saved DataFrame can be later retrieved using :doc:`load_data_frame` operation.

-----
Input
-----
1. DataFrame to save.

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
