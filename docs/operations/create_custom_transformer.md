---
layout: documentation
displayTitle: Create Custom Transformer
title: Create Custom Transformer
description: Create Custom Transformer
usesMathJax: true
includeOperationsMenu: true
---

Create Custom Transformer is an operation used to create a user-defined Transformer.

Custom transformer can be created by building an internal workflow from
[operations](../operations.html). The internal workflow must have at least two nodes:

* input node ("source") which provides an input DataFrame
* output node ("sink") which receives the result of executing the transformation on the input
DataFrame

Create Custom Transformer operation enables the user to create the transformation by defining
data flow between input and output nodes. For the internal workflow to be valid, the same
requirements as for regular [workflow](../deeplang.html#workflows) must be met, i.e.:

* it may not contain cycle
* parameters of all operations must be correct
* each input port must have exactly one incoming connection
* classes of entities passed between ports must meet requirements of these ports' type qualifiers

Additionally:

* it should take input DataFrame from input node
* it should output transformed DataFrame to output node

The Transformer produced by the operation can be used as input to e.g. [Transform](transform.html)
operation. When the Transform operation is executed, its input DataFrame is passed to the input node
of the internal workflow of custom transformer. Once the internal workflow is finished, the result
of the output node is returned as the output DataFrame of the Transform operation.

**Since**: Seahorse 1.0.0

## Input

Create Custom Transformer does not take any input.

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
<tr><td><code>0</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>Transformer that allows to apply the operation on other DataFrames using <a href="transform.html">Transform</a></td>
</tr>
</tbody>
</table>
