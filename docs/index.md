---
layout: documentation
displayTitle: Seahorse Quick Start
menuTab: index
title: Quick Start
description: Seahorse documentation homepage
---

The goal of this exercise is very simple.
We have [csv file](/_static/transactions.csv) with apartments prices from 3
cities and we want to calculate average apartment price for each city.

The csv file has 5 columns and 1000 rows (header row and 999 data rows).
Each row provides information about the apartment:
city, number of bedrooms, number of bathrooms, size of the apartment (in square feets) and its price.

    city,beds,baths,sq_ft,price
    CityB,4,1,1294,529377
    CityC,4,2,1418,574485
    CityC,2,1,600,221661
    ...

### Build workflow

<img class="img-responsive" style="float:right" src="./img/quickstart_workflow.png" />

* Go to <a target="_blank" href="{{ site.SEAHORSE_EDITOR_ADDRESS }}">Seahorse Editor</a>
and click **New workflow**
  * Put <code>quickstart</code> in the **Name** section
  * Press the **create** button
* Drag [Read DataFrame](operations/read_dataframe.html) operation
  to your canvas
  * Click on **Read DataFrame** operation - menu on the right will show its parameters
  * Put <code>transactions.csv</code> in the **SOURCE** parameter
* Drag [SQL Expression](operations/sql_expression.html)
   to your canvas
  * Put <code>transactions</code> in the **DATAFRAME ID** parameter
  * Put <code>SELECT city, AVG(price) FROM transactions GROUP BY city</code> in the **EXPRESSION** parameter
* Connect **Read Data Frame** output with **SQL Expression** input

* Press **Save** button from the top menu
* Press **Export** button from the top menu
  * Press **Download** in popup window
  * Workflow file named **quickstart.json** will be downloaded to your machine

### Execute on Apache Spark

* Download [Workflow Executor JAR](/downloads.html)
or [build from source](batch_workflow_executor_overview.html#building-workflow-executor}})
* Download [transactions.csv](/_static/transactions.csv)

The command presented below will execute workflow on the local Apache Spark.
Workflow Executor JAR and transactions.csv have to be placed in current working directory.
Replace `./bin/spark-submit` with path to script in Apache Spark's directory.
For more details or information on how to run the workflow on real cluster, please check
[this page](workflowexecutor.html#how-to-run-workflow-executor).

    ./bin/spark-submit \
      --class io.deepsense.workflowexecutor.WorkflowExecutorApp \
      --master local[2] \
      workflowexecutor_2.10-latest.jar \
        --workflow-filename quickstart.json \
        --output-directory . \
        --report-level medium \
        --upload-report

### View the reports

<img style="float:right" src="./img/quickstart_report.png" />
When spark-submit job has finished, reports will be available in the Seahorse Editor.
Go to the page with workflow canvas and press the **LAST EXECUTION REPORT** button in the top menu.
The report will contain all the operations execution times and details about the
produced entities.
Click on the output port of
[SQL Expression](operations/sql_expression.html)
to see the results of the query.

### Where to go from here

* [More examples](examples.html): Data processing and Machine Learning examples
