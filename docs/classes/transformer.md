---
layout: global
displayTitle: Transformer
title: Transformer
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

A `Transformer` is an abstraction of direct data processing. It consumes a [DataFrame](../classes/dataframe.html) and produces a [DataFrame](../classes/dataframe.html).

`Transformers` can be executed using a [Transform](../operations/transform.html) operation.

<div class="centered-container" markdown="1">
  ![Transformer usage diagram](../img/transformer.png){: .centered-image .img-responsive .spacer}
  *Transformer usage diagram*
</div>

## Example

A [Tokenize](../operations/tokenize.html) is an operation that outputs a transformed `DataFrame` on its left output port
and a `StringTokenizer` (a `Transformer`) on its right output port. Passing a `Transformer`
to a `Transform` operation allows to perform the `StringTokenizer` on another `DataFrame`.

![transformer example](../img/transformer_example.png){: .img-responsive .centered-image .spacer}
