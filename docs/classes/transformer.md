---
layout: documentation
displayTitle: Transformer
title: Transformer
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

Transformer is an abstraction of direct data processing. It consumes a [DataFrame](../classes/dataframe.html) and produces a [DataFrame](../classes/dataframe.html).

Transformers can be executed using [Transform](../operations/transform.html) operation.

![transformer diagram](../img/transformer.png){: .img-responsive .centered-image .spacer}

## Example

[Tokenize](../operations/tokenize.html) is an operation that outputs transformed DataFrame on its left output port
and a StringTokenizer Transformer on its right output port. Passing a Transformer
to the Transform operation allows to perform the StringTokenizer on another DataFrame.

![transformer example](../img/transformer_example.png){: .img-responsive .centered-image .spacer}
