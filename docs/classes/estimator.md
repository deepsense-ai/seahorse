---
layout: global
displayTitle: Estimator
title: Estimator
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

An `Estimator` is a generalization of a machine learning algorithm. It consumes a
[DataFrame](../classes/dataframe.html) and produces a
[Transformer](../operations/transform.html).
`Estimators` can be executed using a [Fit](../operations/fit.html) operation.

<div class="centered-container" markdown="1">
  ![Estimator usage diagram](../img/estimator.png){: .centered-image .img-responsive}
  *Estimator usage diagram*
</div>

## Example

A [Logistic Regression](../operations/logistic_regression.html) is an operation that outputs an untrained model, which is an `Estimator`.
It is passed to a `Fit` operation. We get a trained model as a result of fitting.
The trained model is a `Transformer` that can be used in a [Transform](../operations/transform.html) operation for scoring on new data.

![estimator example](../img/estimator_example.png){: .img-responsive .centered-image .spacer}
