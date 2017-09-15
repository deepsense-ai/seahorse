---
layout: documentation
displayTitle: Parameters
docTab: parameters
title: Parameters
description: Parameters documentation
---

Each parameter has a name, description and a type. A type of a parameter defines what kind of value it receives (along with some validation logic).

A group of named parameters is called _parameters schema_.

Each [operation](deeplang.html#operations) defines its parameters schema. In order for an operation to be executed, all parameters in its parameters schema have to be filled with valid values.
There are several predefined types of parameters in Deeplang:

### Numeric
Holds single numeric value. It can specify validation rules such as a range of accepted values etc.

**Example of use:** number of iterations of a machine learning algorithm

**Example value:** 100.0

### String
Holds single string value. It can be restricted by a regular expression that has to be satisfied by the input value.

Placeholder strings can be used inside String parameters, e.g. ``${username}`` or ``${database.host}``.
The placeholders will be replaced by actual values based on
[extra variables](workflowexecutor.html#command-line-parameters-details) passed as command-line arguments.

**Example of use:** source parameter  in `ReadDataFrame` operation

**Example value:** “file:///tmp/example.csv”


### Boolean
Represents a simple choice - yes or no.

**Example of use:** should the operation that creates a `DataFrame` from a CSV file extract names of columns from the first row?

**Example value:** true

### <a name="single_column_selector"></a>Single Column Selector
Serves to specify a single column of a [DataFrame](classes/dataframe.html). Column can be specified by its name or its index (starting from 0).

**Example of use:** column to be treated as label in supervised learning algorithm

**Example value:** name: "price"

### <a name="multiple_column_selector"></a>Multiple Column Selector
Serves to specify a subset of columns of a [DataFrame](classes/dataframe.html). Columns can be specified by their names, indices (starting from 0) or types. Therefore, one selector can contain multiple names, index ranges and column types selections. All columns that meet a condition from at least one input selection will be included in the final subset of columns.
The multiple column selector exposes additional boolean flag: _excluding_, which - if selected, changes selecting logic: the final subset contains all columns that do _not_ meet condition from any of input selections.

**Example of use:** columns to be projected in the `Project Column` operation

**Example value:** excluding: false, names: {"a", "b"}, ranges: {1-5, 10-14}, types: {`numeric`, `boolean`}

### <a name="single_choice"></a>Single Choice
A parameter of this type defines a set of options. Exactly one option must be selected. Additionally, each option can have an internal parameters schema - a group of parameters that should be filled if this particular option is selected.

**Example of use:** a target type of conversion in operation converting types of columns of a [DataFrame](classes/dataframe.html)

**Example of use:** In the `Cross Validate` operation, user can specify if he wants additional shuffling to be performed. The single choice parameter `Shuffle` exposes two options: `Yes` and `No`. If user selects `Yes`, she additionally has to provide value for numeric parameter `Seed`.

### <a name="multiple_choice"></a>Multiple Choice
This parameter is similar to [single choice selector](#single_choice), but here user is allowed to select multiple options at once. User does not have to select any option.
Like in single choice selector, each option can have an internal parameters schema - a group of parameters that should be filled if this particular option is selected.

**Example of use:** parts of DateTime on which the selected column is to be split in the `Decompose Datetime` operation.


### Code Snippet
Holds code snippet string value. Different languages (language is choosed by operation creator) can be used to syntax validation and syntax highlighting.

**Example of use:** code snippet parameter for Python language in `CustomOperation` operation

**Example value:**

    def operation_main(data_frame_1):
      return data_frame_1


### <a name="parameters_sequence"></a>Parameters Sequence
This parameter can be used when operation requires user to specify the same group of parameters multiple times, possibly with different values. The parameters sequence defines an internal parameters schema. User can provide any number of sets of values for this schema.

**Example of use:** In the `Join` operation, user has to specify what columns to use in join. More precisely, she can provide any number of pairs of columns, each consisting of one column from left `DataFrame` and one column from right one.

**Example value:** (left column = name: "x", right column = index: 3), (left column = name: "y", right column = name: "x")
