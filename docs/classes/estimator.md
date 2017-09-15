---
layout: documentation
displayTitle: Estimator
title: Estimator
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

$$Estimator: DataFrame \rightarrow Transformer$$

Estimator is a generalization of machine learning algorithm. It consumes a
[DataFrame](../classes/dataframe.html) and produces a
[Transformer](../operations/transform.html).

Estimators can be executed by **Fit** generic operation.

## Example

**Logistic Regression** is an operation that outputs an untrained model, which is an `Estimator`.
It is passed to **Fit** operation. We get a trained model as result of fitting.
The trained model is a `Transformer` that can be used in **Transform** operation for scoring on new data.

![estimator example](../img/estimator_example.png){: .img-responsive}
