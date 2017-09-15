---
layout: documentation
displayTitle: Normalizer
title: Normalizer
includeOperablesMenu: true

description: Seahorse documentation homepage
---

Normalizer is an entity containing data used for normalization of
[DataFrames](dataframe.html).

It is a common use case to
[create Normalizer using training dataset](../operations/train_normalizer.html)
and use that Normalizer to adjust test dataset.
Normalizer holds data (e.g. calculated means and variances of DataFrame columns) that will
allow to uniformly normalize multiple datasets at any point in the workflow.
