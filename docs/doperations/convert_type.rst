.. Copyright (c) 2015, CodiLime, Inc.

Convert Type
============

==========
Descriptor
==========

Converts the selected columns to a desired type.

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
     - DataFrame with the converted columns



===========
Description
===========
An operation to convert types of columns in a DataFrame. It can convert one or more columns at once.
All of the selected columns are converted to the same (selected) type. The conversion is done in
place.
Supports conversions from all types to String type, Numeric type and Categorical type.
When a non-string column is converted to Categorical it is first converted to String.
Every null value stays a null value in the result DataFrame (despite the column type change).
When a Timestamp column is converted to Numeric then each value is represented
by the number of milliseconds since 1 January 1970.
Boolean converted to String generates a column of 'true' and 'false' strings.
Categorical can be converted to Numeric as long as all categories' names are string representation
of a numeric value.
String column can be converted to Numeric as long as all values in the column represent a numeric
value.
A column converted to its type is not modified.
If one or more column can not be converted,
the operation will fail at runtime with TypeConversionException.

------
Params
------
1. ``selectedColumns: MultipleColumnSelector`` - columns to convert.
Even if one of the columns is selected more than once (eg. by name and by type)
it will be included only once. When a column selected by name
or by index does not exist the operation will fail at runtime with ColumnsDoNotExistException.

2. ``targetType: Choice`` - target type of the conversion

  - ``numeric``
  - ``string``
  - ``categorical``



