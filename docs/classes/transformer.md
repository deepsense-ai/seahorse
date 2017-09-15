---
layout: documentation
displayTitle: Transformer
title: Transformer
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

$$Transformer: DataFrame \rightarrow DataFrame$$

Transformer is an abstraction of direct data processing. It consumes a [DataFrame](../classes/dataframe.html) and produces a [DataFrame](../classes/dataframe.html).

Transformers can be executed by **Transform** higher-order operation.

## Example

**Tokenize** is an operation that outputs transformed `DataFrame` on its left output port
and a **StringTokenizer** `Transformer` on its right output port. Passing `Transformer`
to **Transform** allows to perform **Tokenize** on another `DataFrame`.

![transformer example](../img/transformer_example.png){: .img-responsive}
