.. Copyright (c) 2015, CodiLime Inc.

One Hot Encoder
===============

==========
Descriptor
==========

One-hot encodes categorical columns from DataFrame.

**Operation version:** 1

**Since:** DeepSense 0.1.0

-----
Input
-----
.. list-table::
  :widths: 15 30 55
  :header-rows: 1

  * - Port
    - Data Type
    - Description
  * - 0
    - DataFrame
    - DataFrame with columns to encode

------
Output
------
.. list-table::
  :widths: 15 30 55
  :header-rows: 1

  * - Port
    - Data Type
    - Description
  * - 0
    - DataFrame
    - DataFrame with columns containing encoding appended

===========
Description
===========
Operation encodes selected categorical columns so that they are decomposed to numerical columns
containing only 0's and 1's.
Encoding is performed for each selected column.
For each category level :math:`x` of categorical column's categories, additional column is appended.
This additional column has value 1 in particular row if and only if this row has category level
:math:`x`.
Note that column corresponding to last category level can be dropped without information loss.
For example, if column ``Kind`` has three category levels: ``Mammal``, ``Bird`` and ``Fish``,
then if some row has ``0`` both in columns ``Kind_Bird`` and ``Kind_Fish``, it then implies that it
is ``Mammal``.
This redundant column can be either dropped or preserved
- this can be set through ``with redundant`` parameter.

Sets of columns will be appended in order of corresponding selected columns in original DataFrame.
Columns for each category will be appended in lexicographical order of this category levels.

For rows that have ``null`` in categorical column that is being decomposed, it will have ``null``'s
in corresponding columns.

Result columns will have name of column category they represent with name of corresponding
level appended. If the value for parameter ``prefix`` is provided, it will be prepended to
generated columns' names, according to the pattern: ``prefix + columnName + "_" + value``.

Encoded column will not be removed from DataFrame.

------
Params
------

1. ``columns: MultipleColumnSelector`` - subset of the DataFrame columns to encode.
   If columns selected by the user have type different than Categorical,
   ``WrongColumnTypeException`` will be thrown.
   If some of selected columns do not exist,
   ``ColumnDoesNotExistException`` will be thrown.
2. ``with redundant: Boolean`` - if ``true``, column that represents
   last (lexicographically) level of category will be present in result
   - otherwise it will be dropped.
3. ``prefix: PrefixBasedColumnCreatorParameter`` - optional prefix for created columns.
   If provided, names of generated columns match pattern: ``prefix + columnName + "_" + value``.

=======
Example
=======

----------
Input Data
----------

========= ======= ======
Animal    Kind    Size
========= ======= ======
Cow       Mammal  Big
Ostrich   Bird    Big
Trout     Fish    null
Sparrow   Bird    Small
Thing     null    Small
========= ======= ======

----------------
Operation Params
----------------
1. columns = ["Kind", "Size"]
2. with redundant = false
3. prefix = "Animal_"

-----------
Output Data
-----------

========= ======= ====== ================= ================= ================
Animal    Kind    Size   Animal_Kind_Bird  Animal_Kind_Fish  Animal_Size_Big
========= ======= ====== ================= ================= ================
Cow       Mammal  Big    0                 0                 1
Ostrich   Bird    Big    1                 0                 1
Trout     Fish    null   0                 1                 null
Sparrow   Bird    Small  1                 0                 0
Thing     null    Small  null              null              0
========= ======= ====== ================= ================= ================

----------------
Operation Params
----------------
1. columns = ["Kind"]
2. with redundant = true

-----------
Output Data
-----------

========= ======= ====== ========== ========== ============
Animal    Kind    Size   Kind_Bird  Kind_Fish  Kind_Mammal
========= ======= ====== ========== ========== ============
Cow       Mammal  Big    0          0          0
Ostrich   Bird    Big    1          0          0
Trout     Fish    null   0          0          1
Sparrow   Bird    Small  1          0          0
Thing     null    Small  null       null       null
========= ======= ====== ========== ========== ============
