.. Copyright (c) 2015, CodiLime, Inc.

.. DEVELOPERS ONLY ?

Load DataFrame
==============

==========
Descriptor
==========

Loads DataFrame previously written in DeepSense.io.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----
None

------
Output
------

.. list-table:: Output
   :widths: 15 20 65
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - DataFrame
     - DataFrame load from DeepSense.io


===========
Description
===========
Operation to retrieve DataFrame previously written in DeepSense.io
(using for example :doc:`save_data_frame`).

-----
Input
-----
None

------
Output
------

1. Load DataFrame

------
Params
------
.. only:: developers

  1. ``id: String`` - unique id of the DataFrame. Operation assumes that id is a
  correct id of an existing DataFrame.

.. only:: clients

  None


