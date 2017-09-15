---
layout: global
menuTab: downloads
description: Seahorse downloads page
title: Try Seahorse
---

One of the main goals for Seahorse is to make it accessible to new users.
We provide several ways to quickly try Seahorse out.
Depending on your preferences or rules in your organization, you may choose:

<section class="deployment-section">
  <div class="row white">
    <div class="block">
      <div class="col-xs-12 col-sm-6 col-md-4">
        <ul class="deployment">
          <li class="header">
            <div class="wrapper">
              <div>
                <img src="/img/logo.png" height="25px" alt="" />
              </div>
              <div style="height:2em">
                <big>Seahorse Desktop</big>
              </div>
            </div>
          </li>
          <li>Download a virtual image</li>
          <li>Contains all the components</li>
          <li>Includes single-node Apache Spark distribution</li>
          <li><a href="desktop_overview.html">Read more...</a></li>
          <li>
            <a target="_blank" href="//deepsense.io/get-seahorse" class="btn btn-primary active">Download</a>
          </li>
        </ul>
      </div>
      <div class="col-xs-12 col-sm-6 col-md-4">
        <ul class="deployment">
          <li class="header">
            <div class="wrapper">
              <div>
                <img src="../img/tap.png" alt="TAP" height="25px" />
              </div>
              <div>
                <big>Seahorse on Trusted Analytics Platform</big>
              </div>
            </div>
          </li>
          <li>Build TAP platform customized to your organization's needs</li>
          <li>Use Seahorse on Yarn Clusters of any size</li>
          <li>Use OAuth to manage access to Seahorse</li>
          <li><a href="tap_overview.html">Read more...</a></li>
          <li>
            <a target="_blank" href="http://trustedanalytics.org/" class="btn btn-primary active">Visit TAP </a>
          </li>
        </ul>
      </div>
      <div class="col-xs-12 col-sm-6 col-md-4">
        <ul class="deployment">
          <li class="header">
            <div class="wrapper">
              <div>
                <img src="/img/ibm_workbench.png" height="25px" alt="" />
              </div>
              <div>
                <big>Seahorse on IBM Data Scientist Workbench</big>
              </div>
            </div>
          </li>
          <li>Create an account on Data Scientist Workbench</li>
          <li>Start using Seahorse on provided virtual machine</li>
          <li>No download required</li>
          <li><a href="dswb_overview.html">Read more...</a></li>
          <li>
            <a target="_blank" href="https://datascientistworkbench.com/" class="btn btn-primary active">Create account</a>
          </li>
        </ul>
      </div>
    </div>
  </div>
</section>

Scaling up your Seahorse deployment in a production setting is covered in the
[Enterprise](../enterprise.html) section.

Regardless of which Seahorse deployment version you choose for building a Spark application,
after it's completed, it can be [deployed on a production cluster](#spark-application-deployment).

### Spark Application Deployment

Seahorse makes it possible to build Spark applications interactively,
working directly with data in a running session. Your final workflow can be then exported
as a standalone Spark application that you can submit to any Spark cluster using
[Seahorse Batch Workflow Executor](/internal/batch_workflow_executor_overview.html).
A list of precompiled binaries is available below.

<p style="text-align: center; font-style: italic">Version Matrix for Seahorse Batch Workflow Executor</p>


| **Seahorse Batch Workflow Executor Version** | **Apache Spark Version** | **Scala Version** | **Link** |
| 1.2.0 | 1.6 | 2.10 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.2.0/workflowexecutor_2.10-1.2.0.jar">download</a> |
| 1.2.0 | 1.6 | 2.11 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.2.0/workflowexecutor_2.11-1.2.0.jar">download</a> |
| 1.1.0 | 1.6 | 2.10 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.1.0/workflowexecutor_2.10-1.1.0.jar">download</a> |
| 1.1.0 | 1.6 | 2.11 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.1.0/workflowexecutor_2.11-1.1.0.jar">download</a> |

### Batch Workflow Executor Source Code

If you are interested in compiling Seahorse Batch Workflow Executor from source or working with the newest
bleeding-edge code, you can check out the master branch from our Git repository:

```
git clone {{ site.WORKFLOW_EXECUTOR_GITHUB_URL }}
```

## Release Notes

### Seahorse 1.2

#### What is New
* Added operations:
  * Filtering:
    * [Projection](docs/1.2/operations/projection.html)
  * Transformation:
    * Feature Conversion:
      * [Get From Vector](docs/1.2/operations/get_from_vector.html)
    * Model Evaluation
      * [Python Evaluator](docs/1.2/operations/python_evaluator.html)
* Extended [Split](docs/1.2/operations/split.html) and [Handle Missing Values](docs/1.2/operations/handle_missing_values.html) operations
* Improved operations on vectors
* Improved reports formatting

### Seahorse 1.1

#### What is New
* Introduced model exporting.
* Added operations:
  * Input/Output:
    * [Read Transformer](docs/1.1/operations/read_transformer.html)
    * [Write Transformer](docs/1.1/operations/write_transformer.html)
  * Machine Learning:
    * Classification:
      * [Decision Tree Classifier](docs/1.1/operations/decision_tree_classifier.html)
      * [Multilayer Perceptron Classifier](docs/1.1/operations/multilayer_perceptron_classifier.html)
      * [Naive Bayes](docs/1.1/operations/naive_bayes.html)
      * [Random Forest Classifier](docs/1.1/operations/random_forest_classifier.html)
    * Clustering:
      * [LDA](docs/1.1/operations/lda.html)
    * Feature Selection:
      * [Chi-Squared Selector](docs/1.1/operations/chi-squared_selector.html)
    * Model Evaluation:
      * [Binary Classification Evaluator](docs/1.1/operations/binary_classification_evaluator.html)
        * F1 Score
        * Precision
        * Recall
    * Regression:
      * [AFT Survival Regression](docs/1.1/operations/aft_survival_regression.html)
      * [Decision Tree Regression](docs/1.1/operations/decision_tree_regression.html)
    * Transformation:
      * Custom:
        * SQL:
          * [SQL Column Transformation](docs/1.1/operations/sql_column_transformation.html)
      * Feature Conversion:
        * [Quantile Discretizer](docs/1.1/operations/quantile_discretizer.html)
* Removed operations:
  * Transformation:
    * Feature Conversion:
      * [Execute Mathematical Transformation](docs/1.0/operations/execute_mathematical_transformation.html) - replaced with more generic `SQL Column Transformation`.
* Added a possibility to clone an existing workflow.
* Introduced <a target="_blank" href="http://nvd3.org/">NVD3</a> library for charts presentation.
* Added inference in [Custom Transformer](docs/1.1/operations/create_custom_transformer.html).

#### Known Issues
* [SQL Transformation](docs/1.1/operations/sql_transformation.html) does not accept some valid identifiers as `DATAFRAME ID` parameter value.
The problem is caused by an Apache Spark bug <a target="_blank" href="https://issues.apache.org/jira/browse/SPARK-12982">SPARK-12982</a>.
* [Fit](docs/1.1/operations/fit.html) on [Linear Regression](docs/1.1/operations/linear_regression.html) can fail with
`java.lang.AssertionError: assertion failed: lapack.dpotrs returned 65`.
This problem is related to an Apache Spark issue <a target="_blank" href="https://issues.apache.org/jira/browse/SPARK-11918">SPARK-11918</a>.
The workaround is to change `SOLVER` param of `Linear Regression` to **l-bfgs**.



### Seahorse 1.0

#### What is New
* Introduced interactive session support.
* Added a possibility to execute selected operations only.
* Redesigned the Operation Catalogue to reflect the structure of Spark ML.
* Integrated <a target="_blank" href="http://jupyter.org/">Jupyter Notebook</a>.
* Introduced custom code execution by a [Custom Python Operation](docs/1.0/operations/custom_python_operation.html)
and a [Custom Python Column Operation](docs/1.0/operations/custom_python_column_operation.html).
* Introduced [Custom Transformers](docs/1.0/operations/create_custom_transformer.html).
* Added a [Grid Search](docs/1.0/operations/grid_search.html) operation for automatic hyperparameter tuning.
* Bundled set of workflow examples.

#### Known Issues
* [String Indexer](docs/1.0/operations/string_indexer.html), when operating on 1 column, returns a faulty `SingleStringIndexerModel`
which cannot be supplied to a [Transform](docs/1.0/operations/transform.html) operation. However, the `StringIndexer`
still can be used to transform a [DataFrame](docs/1.0/classes/dataframe.html).
* `Execute Mathematical Transformation` fails if executed on a `DataFrame` containing columns
with certain chars (e.g. spaces) in their names.
