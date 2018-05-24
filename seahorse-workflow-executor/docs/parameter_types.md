---
layout: global
displayTitle: Parameter Types
menuTab: reference
title: Parameter Types
description: Parameters Reference
---

Each parameter has a name, description and a type. A type of a parameter defines what kind of value it receives (along with some validation logic).

Each [operation](operations.html) defines its parameters. In order for an `operation` to be executed, all its parameters have to be filled with valid values.
There are several predefined types of parameters in Seahorse:

### Numeric
Holds a single numeric value. It can specify validation rules such as a range of accepted values etc.

**Example of use:** number of iterations of a machine learning algorithm.

**Example value:** `100.0`.

### Multiple Numeric
Holds multiple numeric values. It can specify validation rules such as a range of accepted values etc.

**Example of use:** layer sizes in a multilayer perceptron classifier

**Example value:** [1, 2, 3]

### String
Holds single string value. It can be restricted by a regular expression that has to be satisfied by the input value.

**Example of use:** source parameter in [Read DataFrame](operations/read_dataframe.html) operation.

**Example value:** `file:///tmp/example.csv`.

### Boolean
Represents a simple choice - yes or no.

**Example of use:** should the operation that creates a [DataFrame](classes/dataframe.html) from a CSV file extract names of columns from the first row?

**Example value:** `true`.

### Single Column Selector
Serves to specify a single column of a `DataFrame`. Column can be specified by its name or its index (starting from 0).

**Example of use:** column to be treated as label in supervised learning algorithm.

**Example value:** name: `"price"`.

### Multiple Column Selector
Serves to specify a subset of columns of a `DataFrame`. Columns can be specified by their names,
indices (starting from 0) or types. Therefore, one selector can contain multiple names,
index ranges and column types selections.
All columns that meet a condition from at least one input selection will be included in the final subset of columns.
The multiple column selector exposes additional boolean flag: _excluding_, which - if selected,
changes selecting logic:
the final subset contains all columns that do _not_ the meet condition from any of the input selections.

**Example of use:** columns to be filtered in the [Filter Columns](operations/filter_columns.html) operation.

**Example value:** excluding: `false`, names: {`"a"`, `"b"`}, ranges: {`1-5`, `10-14`}, types: {`numeric`, `boolean`}.

### Input Output Column Selector
Serves to specify input and output columns for an operation.

There are two input column modes:

* `one column` - operate on one input column and output to one column.
* `multiple columns` - operate on multiple input columns and output to many columns.

In both modes, input column(s) and output generation mode have to be specified.

Available output generation modes are:

* `replace input column(s)` - output goes into input columns and replaces previous values.
* `append new column(s)` - output goes into new columns that are appended to the `DataFrame`.

For `one column` input mode with `append new column` output mode, a name for the newly created column
has to be specified.

For `multiple columns` input mode with `append new columns` output mode, `column name prefix` for
the newly created columns has to be specified. Every new column will have a corresponding input
column's name with given prefix prepended.

#### Example of Use

Operation [Convert Type](operations/convert_type.html)

Input DataFrame columns: `a`, `b`, `c`

Parameters:

* operate on = `multiple columns`
* input columns = `{ a, b, c }`
* output mode = `append new columns`
* column name prefix = `"converted_"`

Output DataFrame columns: `a`, `b`, `c`, `converted_a`, `converted_b`, `converted_c`

### Single Choice
A parameter of this type defines a set of options. Exactly one option must be selected. Additionally, each option can have internal parameters that should be filled if this particular option is selected.

**Example of use:** a target type of conversion in operation converting types of columns of a `DataFrame`.

**Example of use:** In the `Read DataFrame` operation, the user should specify `data storage type` parameter. Some possible values of storage type are `FILE` and `JDBC`.

### Multiple Choice
This parameter is similar to [single choice selector](#single-choice), but here the user is allowed to select multiple options at once. User does not have to select any option.
Like in a `single choice selector`, each option can have internal parameters that should be filled if this particular option is selected.

**Example of use:** parts of DateTime on which the selected column is to be split in the [Decompose Datetime](operations/decompose_datetime.html) operation.

### Code Snippet
Holds code snippet string value. Different languages (language is chosen by operation creator) can be used for syntax validation and syntax highlighting.

**Example of use:** code snippet parameter for Python language in [Python Transformation](operations/python_transformation.html).

**Example value:**

{% highlight python %}
def operation_main(data_frame_1):
  return data_frame_1
{% endhighlight %}

### JSON
This parameter holds a JSON object.

**Example of use:** description of the transformation in [Create Custom Transformer](operations/create_custom_transformer.html) operation.

**Example value:**

{% highlight json %}
{
  "field": "value",
  "array": [ "value1", "value2" ]
}
{% endhighlight %}

### Report Type 
This parameter defines what will be reports content. Choosing `metadata report` might mean that there is no need for additional calculation. 
`Extended report` might require additional time to compute.

### Parameters Sequence
This parameter can be used when the operation requires the user
to specify the same group of parameters multiple times, possibly with different values.
The parameters sequence defines an internal parameters schema.
User can provide any number of sets of values for this schema.

**Example of use:** Example of use: In the [Join](operations/join.html) operation,
the user has to specify what columns to use in join.
More precisely, this operation can provide any number of pairs of columns,
each consisting of one column from left `DataFrame` and one column from the right one.

**Example value:** (left column = name: `"x"`, right column = index: `3`), (left column = name: `"y"`, right column = name: `"x"`).
