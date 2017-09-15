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


## Example 3 - Analyze data using Notebook

The goal of this excercise is to show how to use the [Notebook](operations/notebook.html)
and interactively analyze data.

### Build workflow

<img class="img-responsive" style="float:right; padding: 1em;" src="./img/examples_workflow3.png" />

After setting up [Seahorse Bundled Image](/index.html), go to your locally hosted
<a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">Seahorse Editor</a>.

* Create an empty workflow
  * Click **New workflow**
  * Put <code>notebook1</code> in the **Name** section
  * Press the **create** button

* Drag [Read DataFrame](operations/read_dataframe.html) operation
  to your canvas
  * Click on **Read DataFrame** operation - menu on the right will show its parameters
  * Put <code>https://s3.amazonaws.com/workflowexecutor/examples/data/grades.csv</code> in the **SOURCE** parameter
* Drag [Custom Python Operation](operations/custom_python_operation.html) to your canvas
  * Put the following code in the **CODE** parameter:

{% highlight python %}
import json
from pyspark.sql.types import *

def transform(dataframe):
    def get_fce(js):
        return float(json.loads(js)['Cambridge']['FCE'])

    sqlContext.registerFunction(
      "get_fce", get_fce, FloatType())
    sqlContext.registerDataFrameAsTable(dataframe, "df")
    return sqlContext.sql(
      "SELECT Math, English, get_fce(Certificates) as FCE FROM df")
{% endhighlight %}

* Drag [Notebook](operations/notebook.html) to your canvas
* Connect operations as presented in the picture

### Execute and edit notebook

* Select the created Notebook node and click **run** button
* Once the workflow execution is finished, select Notebook node and click **edit code** in the right panel

#### Use Spark Context

The Spark Context is accessible in the notebook as a global variable `sc`.

<table>
<tr>
<td><b>In:</b></td>
<td>
{% highlight python %}
sc.parallelize([1,2,3,4,5]).map(lambda x: x*x).collect()
{% endhighlight %}
</td>
</tr>

<tr>
<td><b>Out:</b></td>
<td>
[1, 4, 9, 16, 25]
</td>
</tr>
</table>

#### Use SQLContext

SQLContext can be accessed as a global variable `sqlContext`.

<table>
<tr>
<td><b>In:</b></td>
<td>
{% highlight python %}
sqlContext.registerDataFrameAsTable(dataframe(), "notebook_df")
sqlContext.sql("SELECT FCE FROM notebook_df").toPandas().sample(5)
{% endhighlight %}
</td>
</tr>

<tr>
<td><b>Out:</b></td>
<td>
<div>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>FCE</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>143</th>
      <td>54.563766</td>
    </tr>
    <tr>
      <th>169</th>
      <td>77.494507</td>
    </tr>
    <tr>
      <th>183</th>
      <td>70.320213</td>
    </tr>
    <tr>
      <th>64</th>
      <td>53.837051</td>
    </tr>
    <tr>
      <th>663</th>
      <td>66.830711</td>
    </tr>
  </tbody>
</table>
</div>
</td>
</tr>
</table>

#### Perform operations on input DataFrame

You can access the DataFrame passed to the Notebook node on first input port by calling the
`dataframe()` function.

<table>
<tr>
<td><b>In:</b></td>
<td>
{% highlight python %}
dataframe().toPandas().sample(5)
{% endhighlight %}
</td>
</tr>

<tr>
<td><b>Out:</b></td>
<td>
<div>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Math</th>
      <th>English</th>
      <th>FCE</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>758</th>
      <td>48.541864</td>
      <td>67.253461</td>
      <td>50.007278</td>
    </tr>
    <tr>
      <th>224</th>
      <td>48.970637</td>
      <td>60.484968</td>
      <td>78.093758</td>
    </tr>
    <tr>
      <th>232</th>
      <td>37.470484</td>
      <td>76.686701</td>
      <td>58.276207</td>
    </tr>
    <tr>
      <th>449</th>
      <td>55.859676</td>
      <td>64.828651</td>
      <td>75.111809</td>
    </tr>
    <tr>
      <th>970</th>
      <td>31.342785</td>
      <td>72.106684</td>
      <td>70.328934</td>
    </tr>
  </tbody>
</table>
</div>
</td>
</tr>
</table>

#### Visualize data using Pandas and Matplotlib

You can use Matplotlib inside notebook cells to generate plots and visualize your data.

<table>
<tr>
<td><b>In:</b></td>
<td>
{% highlight python %}
import matplotlib.pyplot as plt
%matplotlib inline

df = dataframe().toPandas()
p = df.plot(kind='hist', stacked=True, bins=20)
p.set_ylabel('Frequency')
p.set_xlabel('Score')
{% endhighlight %}
</td>
</tr>

<tr>
<td><b>Out:</b></td>
<td>
<div markdown="1">
<matplotlib.text.Text at 0x7f05649ab390>

![png](img/examples_notebook_plot.png)
</div>
</td>
</tr>
</table>
