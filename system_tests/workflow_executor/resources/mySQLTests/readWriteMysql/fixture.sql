DROP TABLE IF EXISTS read_write_mysql_in;
DROP TABLE IF EXISTS read_write_mysql_out;

CREATE TABLE read_write_mysql_in(string_col VARCHAR(255), numeric_col DOUBLE, categorical_col VARCHAR(255), timestamp_col TIMESTAMP, boolean_col BIT(1));

INSERT INTO read_write_mysql_in(string_col, numeric_col, categorical_col, timestamp_col, boolean_col) VALUES ('str_one', 2.3461, 'A', '2013-08-05 18:19:01', true);
INSERT INTO read_write_mysql_in(string_col, numeric_col, categorical_col, timestamp_col, boolean_col) VALUES ('str_two', 4.1334, 'A', '2013-08-05 18:19:02', false);
INSERT INTO read_write_mysql_in(string_col, numeric_col, categorical_col, timestamp_col, boolean_col) VALUES ('str_three', 7.41, 'B', '2013-08-05 18:19:03', true);
