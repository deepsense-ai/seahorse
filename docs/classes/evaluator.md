---
layout: global
displayTitle: Evaluator
title: Evaluator
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

An `Evaluator` is an abstraction of calculating [DataFrame](../classes/dataframe.html) metrics.

`Evaluators` can be executed using an [Evaluate](../operations/evaluate.html) operation.
It consumes a `DataFrame` and produces a [MetricValue](../classes/metric_value.html).


<div class="centered-container" markdown="1">
  ![Evaluator usage diagram](../img/evaluator.png){: .centered-image .img-responsive}
  *Evaluator usage diagram*
</div>

## Example
A [Regression Evaluator](../operations/regression_evaluator.html) is an operation that outputs an `Evaluator`.
It is passed to an `Evaluate` operation, which calculates regression metrics on the previously scored `DataFrame`.

![evaluator example](../img/evaluator_example.png){: .img-responsive .centered-image .spacer}
