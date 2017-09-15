---
layout: documentation
displayTitle: Evaluator
title: Evaluator
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

Evaluator is an abstraction of calculating [DataFrame](../classes/dataframe.html) metrics. It consumes a DataFrame and produces a [MetricValue](../classes/metric_value.html).

Evaluators can be executed using [Evaluate](../operations/evaluate.html) operation.

![evaluator diagram](../img/evaluator.png){: .img-responsive .centered-image .spacer}

## Example
[Regression Evaluator](../operations/regression_evaluator.html) is an operation that outputs an Evaluator. It is passed to Evaluate operation,
which calculates regression metrics on a previously scored DataFrame.

![evaluator example](../img/evaluator_example.png){: .img-responsive .centered-image .spacer}
