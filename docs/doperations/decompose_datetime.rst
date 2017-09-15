.. Copyright (c) 2015, CodiLime Inc.

Decompose Datetime
==================

==========
Descriptor
==========

Operation to extract timestamp parts to a separate columns.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----

.. list-table::
   :widths: 15 20 65
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - DataFrame
     - DataFrame with timestamp column to decompose.


------
Output
------

.. list-table::
   :widths: 15 20 65
   :header-rows: 1

   * - Port
     - Data Type
     - Description
   * - 0
     - DataFrame
     - DataFrame containing new columns with parts of timestamp extracted from
       the original timestamp.


===========
Description
===========
Operation extracts parts of timestamp from a specified timestamp column to newly
created columns.
For example for timestamp column: ``'Birthdate'`` when extracting year part new
column will be created with name: ``'year'``.
If the value for parameter ``prefix`` is provided, it will be prepended to
generated columns' names, according to the pattern: ``prefix + timestampPartName``.


-----
Input
-----
1. DataFrame with timestamp column to decompose.

------
Output
------

1. DataFrame with newly created columns containing parts of timestamp.

------
Params
------
1. ``timestamp column: SingleColumnSelector`` - one of the DataFrame columns.
   If column selected by user has type different then Timestamp, ``WrongColumnTypeException``
   will be thrown.
   If selected column does not exist, ``ColumnDoesNotExistException`` will be thrown.
2. ``parts: MultipleChoice`` - parts of timestamp to extract
   to separate columns.
   Possible values are: ``[year, month, day, hour, minutes, seconds]``.
3. ``prefix: PrefixBasedColumnCreatorParameter`` - optional prefix for created columns.
   If provided, names of generated columns match pattern: ``prefix + timestampPartName``.


=======
Example
=======

----------
Input Data
----------

============= ===================
Name          Birthdate
============= ===================
Jim Morrison  1943-12-08 13:47:16
Jimmi Page    1944-01-09 08:35:10
============= ===================

----------------
Operation Params
----------------
1. timestamp column = Name
2. parts = [year, month, day]

-----------
Output Data
-----------

============= =================== ============== =============== =============
Name          Birthdate           year           month           day
============= =================== ============== =============== =============
Jim Morrison  1943-12-08 13:47:16 1943           12              8
Jimmi Page    1944-01-09 08:35:10 1944           1               9
============= =================== ============== =============== =============

----------------
Operation Params
----------------
1. timestamp column = Name
2. parts = [year, month, day]
3. prefix = "Birthdate_"

-----------
Output Data
-----------

============= =================== ============== =============== =============
Name          Birthdate           Birthdate_year Birthdate_month Birthdate_day
============= =================== ============== =============== =============
Jim Morrison  1943-12-08 13:47:16 1943           12              8
Jimmi Page    1944-01-09 08:35:10 1944           1               9
============= =================== ============== =============== =============

