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
* Python, version 2.7.6
* NumPy, version 1.8.2

### Minimum Hardware Requirements
* 6 GB of a free disk space
* 4 GB of RAM
* Virtualbox must be supported

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
please <a target="_blank" href="http://deepsense.io/about-us/contact/#contact-form">contact us for details</a>.

## Changelog

### Seahorse 1.4

#### What is New

* Support for defining reusable [data sources and sinks](/reference/datasources.html)
* Support for reading and writing from/to Google Spreadsheets
* Support for user-defined operations via [Software Developement Kit](/reference/sdk_user_guide.html)
* Scheduled workflow execution with customizable Notebook-based email notifications
* Completely redesigned UI for building workflows
* Spark 2.0.2 support
* Added operations:
  * [Sort](/1.4/operations/sort.html)
  * [SQL Combine](/1.4/operations/sql_combine.html)

### Seahorse 1.3

#### What is New

* Seahorse has been upgraded to Spark 2.0.0
* Seahorse can connect to external clusters (YARN, Mesos, Standalone)
* R support
* Added library
  * Upload and download datasets between local machine and Seahorse Library
  * Read and write data between Seahorse Library and Spark Cluster
* Added operations:
  * [R Notebook](/1.3/operations/r_notebook.html)
  * [R Column Transformation](/1.3/operations/r_column_transformation.html)
  * [R Transformation](/1.3/operations/r_transformation.html)
  * [R Evaluator](/1.3/operations/r_evaluator.html)
  * [Compose Datetime](/1.3/operations/compose_datetime.html)

### Seahorse 1.2

#### What is New
* Added operations:
  * Filtering:
    * [Projection](/1.2/operations/projection.html)
  * Transformation:
    * Feature Conversion:
      * [Get From Vector](/1.2/operations/get_from_vector.html)
    * Model Evaluation
      * [Python Evaluator](/1.2/operations/python_evaluator.html)
* Extended [Split](/1.2/operations/split.html) and [Handle Missing Values](./operations/handle_missing_values.html) operations
* Improved operations on vectors
* Improved reports formatting

### Seahorse 1.1

#### What is New
* Introduced model exporting.
* Added operations:
  * Input/Output:
    * [Read Transformer](/1.1/operations/read_transformer.html)
    * [Write Transformer](/1.1/operations/write_transformer.html)
  * Machine Learning:
    * Classification:
      * [Decision Tree Classifier](/1.1/operations/decision_tree_classifier.html)
      * [Multilayer Perceptron Classifier](/1.1/operations/multilayer_perceptron_classifier.html)
      * [Naive Bayes](/1.1/operations/naive_bayes.html)
      * [Random Forest Classifier](/1.1/operations/random_forest_classifier.html)
    * Clustering:
      * [LDA](/1.1/operations/lda.html)
    * Feature Selection:
      * [Chi-Squared Selector](/1.1/operations/chi-squared_selector.html)
    * Model Evaluation:
      * [Binary Classification Evaluator](/1.1/operations/binary_classification_evaluator.html)
        * F1 Score
        * Precision
        * Recall
    * Regression:
      * [AFT Survival Regression](/1.1/operations/aft_survival_regression.html)
      * [Decision Tree Regression](/1.1/operations/decision_tree_regression.html)
    * Transformation:
      * Custom:
        * SQL:
          * [SQL Column Transformation](/1.1/operations/sql_column_transformation.html)
      * Feature Conversion:
        * [Quantile Discretizer](/1.1/operations/quantile_discretizer.html)
* Removed operations:
  * Transformation:
    * Feature Conversion:
      * [Execute Mathematical Transformation](/1.0/operations/execute_mathematical_transformation.html) - replaced with more generic `SQL Column Transformation`.
* Added a possibility to clone an existing workflow.
* Introduced <a target="_blank" href="http://nvd3.org/">NVD3</a> library for charts presentation.
* Added inference in [Custom Transformer](/1.1/operations/create_custom_transformer.html).

#### Known Issues
* [SQL Transformation](/1.1/operations/sql_transformation.html) does not accept some valid identifiers as `DATAFRAME ID` parameter value.
The problem is caused by an Apache Spark bug <a target="_blank" href="https://issues.apache.org/jira/browse/SPARK-12982">SPARK-12982</a>.
* [Fit](/1.1/operations/fit.html) on [Linear Regression](/1.1/operations/linear_regression.html) can fail with
`java.lang.AssertionError: assertion failed: lapack.dpotrs returned 65`.
This problem is related to an Apache Spark issue <a target="_blank" href="https://issues.apache.org/jira/browse/SPARK-11918">SPARK-11918</a>.
The workaround is to change `SOLVER` param of `Linear Regression` to **l-bfgs**.



### Seahorse 1.0

#### What is New
* Introduced interactive session support.
* Added a possibility to execute selected operations only.
* Redesigned the Operation Catalogue to reflect the structure of Spark ML.
* Integrated <a target="_blank" href="http://jupyter.org/">Jupyter Notebook</a>.
* Introduced custom code execution by a [Custom Python Operation](/1.0/operations/custom_python_operation.html)
and a [Custom Python Column Operation](/1.0/operations/custom_python_column_operation.html).
* Introduced [Custom Transformers](/1.0/operations/create_custom_transformer.html).
* Added a [Grid Search](/1.0/operations/grid_search.html) operation for automatic hyperparameter tuning.
* Bundled set of workflow examples.

#### Known Issues
* [String Indexer](/1.0/operations/string_indexer.html), when operating on 1 column, returns a faulty `SingleStringIndexerModel`
which cannot be supplied to a [Transform](/1.0/operations/transform.html) operation. However, the `StringIndexer`
still can be used to transform a [DataFrame](/1.0/classes/dataframe.html).
* `Execute Mathematical Transformation` fails if executed on a `DataFrame` containing columns
with certain chars (e.g. spaces) in their names.
