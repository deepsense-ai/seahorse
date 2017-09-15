.. Copyright (c) 2015, CodiLime, Inc.

Read File
=========

==========
Descriptor
==========

Reads file from HDFS to File entity

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
     - File
     - File entity containing lines of a CSV file.


===========
Description
===========
Read File operation reads a text file from HDFS.
Current version of the operation supports only CSV files.

-----
Input
-----
None

------
Output
------
1. File entity representing CSV file

------
Params
------
1. ``path: String`` - path to a CSV file on HDFS
2. ``lineSeparator: Choice`` - delimiter of liness
      - ``windows line separator`` - line separator used on Windows (CR+LF)
      - ``linux line separator`` - line separator used on Unix based systems (LF)
      - ``custom line separator`` - custom line separator
         - ``customLineSeparator: String`` - custom line separator
