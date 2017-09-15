.. Copyright (c) 2015, CodiLime, Inc.

(Left) Join
===========

==========
Descriptor
==========

Creates a new DataFrame by LEFT JOIN by the columns specified that should exist and be of the same
type in two DataFrames with all of the columns from the left DataFrame and the columns of the right
DataFrame but the columns used for LEFT JOIN.

The operations attempts to follow the behaviour of LEFT JOIN in SQL wherever possible.

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
     - Left-side DataFrame
   * - 1
     - DataFrame
     - Right-side DataFrame

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
     - DataFrame containing the columns of the left DataFrame
       and the columns of the right DataFrame but the columns
       used to LEFT JOIN (in ``joinColumns`` parameter)


===========
Description
===========
Join operation creates a new DataFrame that consists of the values in the columns of the left DataFrame
and the columns of the right DataFrame but the columns used to LEFT JOIN. Rows match when the values in
``joinColumns`` do.

* The values must be equal to match.
* Order of the columns is preserved (from left to right).

The operation joins two DataFrames by the columns in ``joinColumns`` that must be in both
DataFrames and of the same type. If ``joinColumns`` are not in either DataFrames or they are
of different types, ``ColumnsDoNotExistException`` or ``WrongColumnTypeException`` exceptions are thrown,
respectively.

The join operation skips ``null`` values in LEFT JOIN, i.e. ``null`` s do not match and yield rows.

Columns from right DataFrame with the names that currently exist in left DataFrame
will be renamed by appending ``'_join'`` suffix.
If this name is occupied, ``'_1'`` will be appended to it
(or ``'_2'``, ``'_3'`` etc. so that uniqueness of column names is preserved).


-----
Input
-----
1. Left-side DataFrame
1. Right-side DataFrame

------
Output
------
1. DataFrame containing the data of the two DataFrame LEFT JOIN'ed with the columns
   of the left DataFrame and a subset of the columns of the right DataFrame.

------
Params
------
1. ``joinColumns: MultipleColumnSelector`` - columns to LEFT JOIN upon.
   Even if one of the columns is selected more than once (eg. by name and by type)
   it will be included only once.
   Empty selection is not supported and exception ```ColumnsDoNotExistException`` is thrown.
   When a column selected by name or by index does not exist in any or both DataFrames,
   ``ColumnsDoNotExistException`` is thrown.
   When the type of columns to LEFT JOIN upon in two DataFrames do not match,
   ``WrongColumnTypeException`` is thrown.
