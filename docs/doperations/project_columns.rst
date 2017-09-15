.. Copyright (c) 2015, CodiLime, Inc.

ProjectColumns DOperation
=========================

==========
Descriptor
==========

Creates a new DataFrame by selecting columns from the input DataFrame.

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
     - DataFrame to select columns from.

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
     - DataFrame containing the selected columns (and only them).


===========
Description
===========
Project Columns operation creates a new DataFrame that consists of (and only of) the selected
columns. Order of the columns is preserved. Each column can be selected only once. Selecting
a column more than once will not have any effect. Thus, in the output DataFrame no column will
be duplicated.

-----
Input
-----
1. DataFrame to select columns from.

------
Output
------
1. DataFrame containing the selected columns (and only them).

------
Params
------
1. ``columns: MultipleColumnSelector`` - columns to be included in the output DataFrame.
Even if one of the columns is selected more than once (eg. by name and by type)
it will be included only once. Empty selection is supported, but when a column selected by name
or by index does not exist the operation will fail at runtime with ColumnsDoesNotExistException.

.. _language_label:

===========
Appendix A.
===========
