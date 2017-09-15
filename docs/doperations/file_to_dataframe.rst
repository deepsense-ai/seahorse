.. Copyright (c) 2015, CodiLime, Inc.

FileToDataFrame
===============

==========
Descriptor
==========

Converts a file to DataFrame.

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
     - File
     - File to be converted to DataFrame.

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
     - DataFrame with contents of the file.


===========
Description
===========
FileToDataFrame operation converts a file to a DataFrame. The operation infers column types.
When a column contains values of different types, the narrowest possible type will be chosen,
so that all the values can be represented in that type.
It is possible to select columns to be Categorical (by index or by name). When categorizing
a non-string column all values will be cast to strings first.

If the file defines column names they will be used in the output DataFrame. Otherwise, column will
be named :math:`column\_x`, where :math:`x` is column's index (starting from 0).

-----
Input
-----
1. A File containing data to be converted to a DataFrame.

------
Output
------
1. A DataFrame converted from the input file.

------
Params
------
1. ``format: Choice`` - Defines the format of the input file.

  - ``CSV`` - Additional parameters for CSV format.
     - ``separator: String`` - Values separator.
     - ``namesIncluded: Boolean`` - Whether the first row include column names or not.

2. ``categoricalColumns: ColumnSelector`` - Columns with Categorical values. Supports selection
by index and name (if names of the columns are known).
