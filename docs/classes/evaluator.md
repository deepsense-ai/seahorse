---
layout: documentation
displayTitle: Evaluator
title: Evaluator
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

$$Evaluator: DataFrame \rightarrow MetricValue$$

Evaluator is an abstraction of calculating `DataFrame` metrics. It consumes a `DataFrame` and produces a `MetricValue`.

Evaluators can be executed by **Evaluate** generic operation.

## Example

**Regression Evaluator** is an operation that outputs an `Evaluator`. It is passed to **Evaluate** operation,
which calculates regression metrics on a previously scored `DataFrame`.

![estimator example](../img/evaluator_example.png){: .img-responsive}
