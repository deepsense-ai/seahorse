---
layout: documentation
displayTitle: Estimator
title: Estimator
usesMathJax: true
includeClassesMenu: true

description: Seahorse documentation homepage
---
## Introduction

Estimator is a generalization of a machine learning algorithm. It consumes a
[DataFrame](../classes/dataframe.html) and produces a
[Transformer](../operations/transform.html).

Estimators can be executed using [Fit](../operations/fit.html) operation.

![estimator diagram](../img/estimator.png){: .img-responsive .centered-image .spacer}

## Example

[Logistic Regression](../operations/logistic_regression.html) is an operation that outputs an untrained model, which is an Estimator.
It is passed to the Fit operation. We get a trained model as a result of fitting.
The trained model is a Transformer that can be used in the [Transform](../operations/transform.html) operation for scoring on new data.

![estimator example](../img/estimator_example.png){: .img-responsive .centered-image .spacer}
