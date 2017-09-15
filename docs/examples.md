---
layout: documentation
displayTitle: Seahorse Examples
menuTab: examples
title: Examples
description: Seahorse documentation homepage
---

## Example 1 - Build a simple regression model

The goal of this exercise is to build a model predicting apartments prices
basing on 3 apartments features:
<code>beds</code>, <code>baths</code> and <code>sq_ft</code>.

The dataset [transactions.csv](/_static/transactions.csv) has 5 columns and 1000 rows
(header row and 999 data rows).
Each row provides information about the apartment:
city, number of bedrooms, number of bathrooms, size of the apartment (in square feets) and its price.

    city,beds,baths,sq_ft,price
    CityB,4,1,1294,529377
    CityC,4,2,1418,574485
    CityC,2,1,600,221661
    ...

To build the model we will split our initial dataset
into two parts - training and validation sets. We will train the
Ridge Regression model using training set.
The model will be scored (against validation part of the dataset) and report of
the scoring will be produced (model performance report).

### Build workflow

<img class="img-responsive" style="float:right" src="./img/examples_workflow1.png" />

* Go to <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">Seahorse Editor</a> and click **New workflow**
  * Put <code>machinelearning1</code> in the **Name** section
  * Press the **create** button

* Drag [Read DataFrame](operations/read_dataframe.html) operation
  to your canvas
  * Click on **Read DataFrame** operation - menu on the right will show its parameters
  * Put <code>transactions.csv</code> in the **SOURCE** parameter
* Drag [Create Ridge Regression](operations/create_ridge_regression.html) to your canvas
  * Put <code>10</code> in the **ITERATIONS NUMBER** parameter
* Drag [Split](operations/split.html) to your canvas
* Drag [Train Regressor](operations/train_regressor.html) to your canvas
  * In the **FEATURE COLUMNS** section:
    * Click **Edit selection**, this will open selection window for **FEATURE COLUMNS** parameter
    * Click at **Names list** and then 2 times at **Add name**, this will result in adding total of 3 empty text fields
    * Put <code>beds</code> in the first field, <code>baths</code> in the second and <code>sq_ft</code> in the third text field
  * In the **TARGET COLUMN** section:
    * Click **Edit selection**, this will open selection window for **TARGET COLUMN** parameter
    * Click at **Single column**, this will add an empty text field
    * Put <code>price</code> in the text field
* Drag [Score Regressor](operations/score_regressor.html) to your canvas
  * Put <code>prediction</code> in the **PREDICTION COLUMN** parameter
* Drag [Evaluate Regression](operations/evaluate_regression.html) to your canvas
  * In the **TARGET COLUMN** section:
    * Click **Edit selection**, this will open selection window for **TARGET COLUMN** parameter
    * Click at **Single column**, this will add an empty text field
    * Put <code>price</code> in the text field
  * In the **PREDICTION COLUMN** section:
    * Click **Edit selection**, this will open selection window for **PREDICTION COLUMN** parameter
    * Click at **Single column**, this will add an empty text field
    * Put <code>prediction</code> in the text field
* Connect operations as presented on the picture

* Press **Save** button from the top menu
* Press **Export** button from the top menu
    * Press **Download** in popup window
    * Workflow file named **machinelearning1.json** will be downloaded on your machine

### Execute on Apache Spark

* Download [Workflow Executor JAR](/downloads.html)
or [build from source]({{site.WORKFLOW_EXECUTOR_DOC_LINK}}#building-workflow-executor}})
* Download [transactions.csv](/_static/transactions.csv)

The command presented below will execute workflow on the Local Apache Spark.
Workflow Executor JAR and transactions.csv have to be placed in current working directory.
Replace `./bin/spark-submit` with path to script in Apache Spark's directory.
For more details or information on how to run the workflow on real cluster, please check
[this page](workflowexecutor.html#how-to-run-workflow-executor)

    ./bin/spark-submit \
      --class io.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master local[2] \
      workflowexecutor_2.10-latest.jar \
        --workflow-filename machinelearning1.json \
        --output-directory . \
        --report-level medium \
        --upload-report

### View the reports

Click on the **LAST EXECUTION REPORT**, and on the report icon under the
**Evaluate Regression**. These metrics are showing our model performance.
In the next example we will try to improve these metrics.

<div class="centered-container" markdown="1">
  ![Evaluate Regression Report](./img/examples_report1.png){: .centered-image .img-responsive}
</div>

## Example 2 - Build a better model

<img class="img-responsive" style="float:right" src="./img/examples_workflow2.png" />

The goal of this exercise is to improve our previous models performance.
In previous example we only used 3 features of the apartments:
<code>beds</code>, <code>baths</code> and <code>sq_ft</code>.
We will add the <code>city</code> feature to the model now.

In our dataset <code>city</code> is a text column,
and [Ridge Regression](operations/create_ridge_regression.html)
algorithm only works on numerical columns.
One of the ways to fix this problem is to use
[Random Forest Regression](operations/create_random_forest_regression.html)
algorithm which can work on the categorical and
numerical columns. We will mark <code>city</code> as categorical in the [Read DataFrame](operations/read_dataframe.html) operation.

### Update workflow

* Open workflow from **Example 1** in the <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">Seahorse Editor</a>
* Click on the [Read DataFrame](operations/read_dataframe.html):
  * In the **CATEGORICAL COLUMNS** section click **Edit selection**
  * Click at **Names List** section
  * Put <code>city</code> in the text field
* Remove [Create Ridge Regression](operations/create_ridge_regression.html) from the canvas
  * Select the operation and press the <code>delete</code> key
* Add [Create Random Forest Regression](operations/create_ridge_regression.html) to the canvas
  * Put <code>10</code> the **NUM TREES** parameter
  * Connect [Create Random Forest Regression](operations/create_ridge_regression.html)
  with [Train Regressor](operations/train_regressor.html)
* Click on the [Train Regressor](operations/train_regressor.html)
  * Add <code>city</code> to the **FEATURE COLUMNS** parameter

* Press **Save** button from the top menu
* Press **Export** button from the top menu
  * Press **Download** in popup window
  * Workflow file named **machinelearning1.json** will be downloaded on your machine

<div style="clear:both" />

### Execute on Apache Spark

* Download [workflow executor jar](/downloads.html)
or [build from source]({{workflowexecutor.html#building-workflow-executor}})
* Download [transactions.csv](/_static/transactions.csv)

Workflow Executor JAR and transactions.csv have to be placed in current working directory.
Replace `./bin/spark-submit` with path to script in Apache Spark's directory.
Execute the following command:

    ./bin/spark-submit \
      --class io.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master local[2] \
      workflowexecutor_2.10-latest.jar \
        --workflow-filename machinelearning1.json \
        --output-directory . \
        --report-level medium \
        --upload-report

### View the reports

Click on the **LAST EXECUTION REPORT**, and on the report icon under the
**Evaluate Regression**. These metrics are showing our model performance.
As you can see the model performance is much better than in previous example.

<div class="centered-container" markdown="1">
  ![Evaluate Regression Report](./img/examples_report2.png){: .centered-image .img-responsive}
</div>
