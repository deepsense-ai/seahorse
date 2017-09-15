---
layout: global
displayTitle: Release Notes
menuTab: reference
title: Release Notes
description: Seahorse Release Notes
---

**Table of Contents**

* Table of Contents
{:toc}

## Technical Sheet

### Bundled Software Packages

* Apache Spark, version {{ site.WORKFLOW_EXECUTOR_SPARK_VERSION }}

* Python, version 2.7.12
* NumPy, version 1.9.3
* scikit-learn, version 0.16.1

* R, version 3.3.1

### Minimum Hardware Requirements For Docker
* 10 GB of a free disk space
* 4 GB of RAM

### Minimum Hardware Requirements For Vagrant
* 40 GB of a free disk space
* 4 GB of RAM
* VirtualBox must be supported

### Local Cluster Limitations

Seahorse works with YARN, Mesos, Spark Standalone and Local clusters.

Seahorse running on bundled Local cluster was tested with datasets of the following shape:

* DataFrame size: 500 MB
* Number of rows in a DataFrame: 4 M
* Number of columns in a DataFrame: 100
* Row size: 1 MB

This is just a reference point. Depending on the exact dataset,
Local cluster may handle more data.
It's recommended to run a single session at a time.
This guarantees that multiple sessions don't compete for limited resources.

To learn more about scaling up and using Seahorse in production,
please <a target="_blank" href="https://deepsense.ai/contact">contact us for details</a>.

## Changelog

### Seahorse 1.4

#### What is New

* Support for defining reusable [data sources and sinks](./data_sources.html)
* Support for reading and writing from/to Google Spreadsheets
* Support for user-defined operations via [Software Developement Kit](./sdk_user_guide.html)
* Scheduled workflow execution with customizable Notebook-based email notifications
* Completely redesigned UI for building workflows
* Spark 2.0.2 support
* Added operations:
  * `Sort`
  * `SQL Combine`

#### Known Issues

* Seahorse Notebooks do not work with linux kernel 3.13.<br />
  We recommend upgrading the kernel to the newer version.

### Seahorse 1.3

#### What is New

* Seahorse has been upgraded to Spark 2.0.0
* Seahorse can connect to external clusters (YARN, Mesos, Standalone)
* R support
* Added library
  * Upload and download datasets between local machine and Seahorse Library
  * Read and write data between Seahorse Library and Spark Cluster
* Added operations:
  * `R Notebook`
  * `R Column Transformation`
  * `R Transformation`
  * `R Evaluator`
  * `Compose Datetime`

### Seahorse 1.2

#### What is New
* Added operations:
  * Filtering:
    * `Projection`
  * Transformation:
    * Feature Conversion:
      * `Get From Vector`
    * Model Evaluation
      * `Python Evaluator`
* Extended `Split` and `Handle Missing Values` operations
* Improved operations on vectors
* Improved reports formatting

### Seahorse 1.1

#### What is New
* Introduced model exporting.
* Added operations:
  * Input/Output:
    * `Read Transformer`
    * `Write Transformer`
  * Machine Learning:
    * Classification:
      * `Decision Tree Classifier`
      * `Multilayer Perceptron Classifier`
      * `Naive Bayes`
      * `Random Forest Classifier`
    * Clustering:
      * `LDA`
    * Feature Selection:
      * `Chi-Squared Selector`
    * Model Evaluation:
      * `Binary Classification Evaluator`
        * F1 Score
        * Precision
        * Recall
    * Regression:
      * `AFT Survival Regression`
      * `Decision Tree Regression`
    * Transformation:
      * Custom:
        * SQL:
          * `SQL Column Transformation`
      * Feature Conversion:
        * `Quantile Discretizer`
* Removed operations:
  * Transformation:
    * Feature Conversion:
      * `Execute Mathematical Transformation` - replaced with more generic `SQL Column Transformation`.
* Added a possibility to clone an existing workflow.
* Introduced <a target="_blank" href="http://nvd3.org/">NVD3</a> library for charts presentation.
* Added inference in `Custom Transformer`.

#### Known Issues
* `SQL Transformation` does not accept some valid identifiers as `DATAFRAME ID` parameter value.
The problem is caused by an Apache Spark bug <a target="_blank" href="https://issues.apache.org/jira/browse/SPARK-12982">SPARK-12982</a>.
* `Fit` on `Linear Regression` can fail with
`java.lang.AssertionError: assertion failed: lapack.dpotrs returned 65`.
This problem is related to an Apache Spark issue <a target="_blank" href="https://issues.apache.org/jira/browse/SPARK-11918">SPARK-11918</a>.
The workaround is to change `SOLVER` param of `Linear Regression` to **l-bfgs**.



### Seahorse 1.0

#### What is New
* Introduced interactive session support.
* Added a possibility to execute selected operations only.
* Redesigned the Operation Catalogue to reflect the structure of Spark ML.
* Integrated <a target="_blank" href="http://jupyter.org/">Jupyter Notebook</a>.
* Introduced custom code execution by a `Custom Python Operation`
and a `Custom Python Column Operation`.
* Introduced `Custom Transformers`.
* Added a `Grid Search` operation for automatic hyperparameter tuning.
* Bundled set of workflow examples.

#### Known Issues
* `String Indexer`, when operating on 1 column, returns a faulty `SingleStringIndexerModel`
which cannot be supplied to a `Transform` operation. However, the `StringIndexer`
still can be used to transform a `DataFrame`.
* `Execute Mathematical Transformation` fails if executed on a `DataFrame` containing columns
with certain chars (e.g. spaces) in their names.
