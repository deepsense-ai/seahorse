---
layout: global
displayTitle: Create Custom Transformer
title: Create Custom Transformer
description: Create Custom Transformer
usesMathJax: true
includeOperationsMenu: true
---

A `Create Custom Transformer` is an operation used to create a user-defined [Transformer](../classes/transformer.html).

The main purpose of creating a custom `Transformer` is to transform multiple [DataFrames](../classes/dataframe.html)
using the same logic.

A Custom transformer can be created by building an internal workflow from
[operations](../operations.html). The internal workflow must have two following nodes:

* input node ("source") which provides an input `DataFrame`,
* output node ("sink") which receives the result of executing the transformation on the input
`DataFrame`.

The `Create Custom Transformer` operation enables the user to create a transformation by defining
a data flow between the input and output nodes. For the internal workflow to be valid, the same
requirements as for a regular workflow must be met, i.e.:

* it must not contain a cycle,
* the parameters of all operations must be correct,
* each input port must have exactly one incoming connection,
* the classes of entities passed between ports must meet requirements of these ports' type qualifiers.

In addition to that:

* it should take the input `DataFrame` from the input node,
* it should output the transformed `DataFrame` to the output node.

The `Transformer` produced by the operation can be used as an input to e.g. a [Transform](transform.html)
operation. When the `Transform` operation is executed, its input `DataFrame` is passed to the input node
of the internal workflow of the custom transformer. Once the internal workflow is finished, the result
of the output node is returned as the output `DataFrame` of the `Transform` operation.

**Since**: Seahorse 1.0.0

## Input

The `Create Custom Transformer` does not take any input.

## Output

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Type Qualifier</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr><td><code>0</code></td>
<td><code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>Transformer</code> that allows to apply the operation on another <code>DataFrames</code>
using a <code><a href="transform.html">Transform</a></code>.</td>
</tr>
</tbody>
</table>

## Parameters

<table class="table">
<thead>
<tr>
  <th style="width:20%">Name</th>
  <th style="width:25%">Type</th>
  <th style="width:55%">Description</th>
</tr>
</thead>
<tbody>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>