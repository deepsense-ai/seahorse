---
layout: documentation
displayTitle: Workflow File Format
docTab: workflowfile
title: Workflow File
includeTechnicalOverviewMenu: true
description: Deepsense documentation homepage
---

**Table of Contents**

* Table of Contents
{:toc}

## Overview

The main purpose of the workflow file is to store information about
[workflow](deeplang_overview.html#workflows) design and results
of its execution. Workflow file uses JSON format.

## Workflow File Format

Workflow file contains up to 5 sections: [metadata](#metadata), [workflow](#workflow),
[variables](#variables), [executionReport](#execution-report),
[thirdPartyData](#third-party-data) and an optional identifier.

{% highlight json %}
{
  "id": "bbf34cbb-9ee6-442f-80a8-22886b860933",
  "metadata": { ... },
  "workflow": { ... },
  "variables": { ... },
  "executionReport": { ... },
  "thirdPartyData": { ... }
}
{% endhighlight %}

### Metadata

Metadata contains information about [workflow](deeplang_overview.html#workflows)
type and Seahorse API version.

| Property | Description |
| --- | --- |
| `type` | Execution mode for the workflow: "batch". |
| `apiVersion` |  Seahorse API version. |

##### Example
{:.no_toc}

{% highlight json %}
"metadata": {
  "type": "batch",
  "apiVersion": "1.0.0"
}
{% endhighlight %}

### Workflow

Workflow section contains a list of nodes and describes connections between them.

#### Nodes

List of nodes with associated [operations](operations.html).

| Property | Description |
| --- | --- |
| ``id`` | Unique identifier of a node in UUID format. |
| ``operation`` | Description of an operation associated with the node. It consists of unique operation identifier and operation name. |
| ``parameters`` | Operation specific parameters formatted according to [parameter format](parameters.html). |

#### Connections

Connections represent flow of data. Data from one operation output port can be connected to another operation input port.

| Property | Subproperty | Description |
| --- | --- | --- |
| ``from`` | | |
| | ``nodeId`` | Node producing data. |
| | ``portIndex`` | Output port index. |
| ``to`` | | |
| | ``nodeId`` | Node consuming data. |
| | ``portIndex`` | Input port index. |


##### Example
{:.no_toc}

{% highlight json %}
"workflow": {
  "nodes": [
    {
      "id": "742743c3-a0b1-41dc-82b1-172d68e1e814",
      "operation": {
        "id": "c48dd54c-6aef-42df-ad7a-42fc59a09f0e",
        "name": "Read DataFrame"
      },
      "parameters": {
        "data storage type": {
          "FILE": {
            "source": "file:///var/input.csv",
            "format": {
              "CSV": {
                "separator": {
                  ",": {

                  }
                },
                "names included": true,
                "convert to boolean": false
              }
            }
          }
        }
      }
    },
    {
      "id": "c20f5b58-4193-11e5-a151-feff819cdc9f",
      "operation": {
	"id": "d273c42f-b840-4402-ba6b-18282cc68de3",
	"name": "Split DataFrame"
      },
      "parameters": {
	"split ratio": 0.7,
	"seed": 1.0
      }
    }
  ],
  "connections": [
    {
      "from": {
	"nodeId": "742743c3-a0b1-41dc-82b1-172d68e1e814",
	"portIndex": 0
      },
      "to": {
	"nodeId": "c20f5b58-4193-11e5-a151-feff819cdc9f",
	"portIndex": 0
      }
    }
  ]
}
{% endhighlight %}

### Execution Report

Execution report contains information about [workflow](deeplang_overview.html#workflows)
execution on the cluster: status information, errors, entity reports.
This section is optional.

| Property | Description |
| --- | --- |
| ``error`` | Error message - present only if the workflow execution failed. |
| ``nodes`` | Described in following [Nodes](#nodes-1) subsection. |
| ``resultEntities`` | Described in following [Result entities](#result-entities) subsection. |

#### Nodes

Subsection *nodes* contains information about [operation](operations.html) execution status.

| Property | Description |
| --- | --- |
| ``status`` | Operation execution status - ``COMPLETED``, ``FAILED`` or ``ABORTED``. |
| ``error`` | Error message - present only if operation execution failed. |
| ``started`` | Timestamp of operation execution start. It contains date, time and timezone according to ISO 8601 standard.<br>Example: ``2015-05-12T21:11:09Z`` |
| ``ended`` | Timestamp of operation execution finish. Format is the same as in ``started``. |
| ``results`` | UUID identifiers of output entities produced by the operation. |

#### Result Entities

Subsection *resultEntities* contains information about entities created as a result of
[workflow](deeplang_overview.html#workflows) execution, such as data frames and models.

| Property | Description |
| --- | --- |
| ``className`` | Class name of described entity. |
| ``report`` | Object containing detailed entity report about each entity. The exact form is dependent on the entity type. |

##### Example
{:.no_toc}

{% highlight json %}
"executionReport": {
  "error": null,
  "nodes": {
    "742743c3-a0b1-41dc-82b1-172d68e1e814": {
      "status": "COMPLETED",
      "started": "2015-05-12T21:11:09Z",
      "ended": "2015-05-14T12:03:55Z",
      "results": [
	      "2518d941-43cb-4288-b959-bc4441b86083",
	      "50570c02-bf62-4352-b68a-69873261a234"
      ],
      "error": null
    },
    "c20f5b58-4193-11e5-a151-feff819cdc9f": {
      "status": "COMPLETED",
      "started": "2015-05-12T21:11:09Z",
      "ended": "2015-05-14T12:03:55Z",
      "results": [
	      "c376d234-b64f-4cec-8c47-6a3e54adf181"
      ],
      "error": null
    }
  },
  "resultEntities": {
    "2518d941-43cb-4288-b959-bc4441b86083": {
      "className": "DataFrame",
      "report": {}
    },
    "50570c02-bf62-4352-b68a-69873261a234": {
      "className": "RegressionModel",
      "report": {}
    },
    "c376d234-b64f-4cec-8c47-6a3e54adf181": {
      "className": "DataFrame",
      "report": {}
    }
  }
}
{% endhighlight %}

### Third Party Data

Third party data can contain any valid JSON. It is neither read nor interpreted
by the [Seahorse Batch Workflow Executor](batch_workflow_executor_overview.html).

##### Example
{:.no_toc}

{% highlight json %}
"thirdPartyData": {
  "customVersioningApp": {
    "version": "2.3.4",
    "author": "John Doe"
  }
}
{% endhighlight %}

## Example of Workflow File

<div class="collapsable-content">
{% highlight json %}
{
  "id": "3b7b6aee-0fe4-4136-8de3-d06a68fbc059",
  "metadata": {
    "type": "batch",
    "apiVersion": "1.0.0"
  },
  "workflow": {
    "nodes": [{
      "id": "051259cb-189f-2706-6f5a-7f684d52c849",
      "operation": {
        "id": "c48dd54c-6aef-42df-ad7a-42fc59a09f0e",
        "name": "Read DataFrame"
      },
      "parameters": {
        "data storage type": {
          "FILE": {
            "source": "~/transactions.csv",
            "format": {
              "CSV": {
                "separator": {
                  ",": {

                  }
                },
                "names included": true,
                "convert to boolean": false
              }
            }
          }
        }
      }
    }, {
      "id": "bf6900e4-eea1-8f6c-6a0e-8d79fd2bb165",
      "operation": {
        "id": "9e460036-95cc-42c5-ba64-5bc767a40e4e",
        "name": "Write DataFrame"
      },
      "parameters": {
        "data storage type": {
          "FILE": {
            "outputFile": "file:///root/filtered_transactions.csv",
            "format": {
              "CSV": {
                "separator": {
                  ",": {

                  }
                },
                "names included": true
              }
            }
          }
        }
      }
    }, {
      "id": "67610468-7121-a364-ac17-68d578dec03b",
      "operation": {
        "id": "6534f3f4-fa3a-49d9-b911-c213d3da8b5d",
        "name": "Filter Columns"
      },
      "parameters": {
        "selected columns": {
          "selections": [{
            "type": "columnList",
            "values": ["sq_ft", "price"]
          }],
          "excluding": false
        }
      }
    }],
    "connections": [{
      "from": {
        "nodeId": "051259cb-189f-2706-6f5a-7f684d52c849",
        "portIndex": 0
      },
      "to": {
        "nodeId": "67610468-7121-a364-ac17-68d578dec03b",
        "portIndex": 0
      }
    }, {
      "from": {
        "nodeId": "67610468-7121-a364-ac17-68d578dec03b",
        "portIndex": 0
      },
      "to": {
        "nodeId": "bf6900e4-eea1-8f6c-6a0e-8d79fd2bb165",
        "portIndex": 0
      }
    }]
  },
  "thirdPartyData": {
    "gui": {
      "name": "My first workflow",
      "description": "Simple example of workflow",
      "predefColors": ["#00B1EB", "#1ab394", "#2f4050", "#f8ac59", "#ed5565", "#DD6D3F"],
      "nodes": {
        "051259cb-189f-2706-6f5a-7f684d52c849": {
          "uiName": "",
          "color": "#00B1EB",
          "coordinates": {
            "x": 5354,
            "y": 5102
          }
        },
        "bf6900e4-eea1-8f6c-6a0e-8d79fd2bb165": {
          "uiName": "",
          "color": "#00B1EB",
          "coordinates": {
            "x": 5358,
            "y": 5430
          }
        },
        "67610468-7121-a364-ac17-68d578dec03b": {
          "uiName": "",
          "color": "#00B1EB",
          "coordinates": {
            "x": 5364,
            "y": 5234
          }
        }
      }
    },
    "notebooks": {

    }
  },
  "executionReport": {
    "resultEntities": {
      "04c20743-a380-42f3-bfa9-7ef648414fb6": {
        "className": "io.deepsense.deeplang.doperables.dataframe.DataFrame",
        "report": {
          "name": "DataFrame Report",
          "reportType": "DataFrameFull",
          "tables": [{
            "name": "Data Sample",
            "description": "Data Sample. Randomly selected 10 rows",
            "columnNames": ["city", "beds", "baths", "sq_ft", "price"],
            "columnTypes": ["string", "numeric", "numeric", "numeric", "numeric"],
            "rowNames": null,
            "values": [["CityA", "2", "1", "820", "449178"], ["CityC", "2", "1", "656", "267975"], ["CityA", "2", "1", "636", "348946"], ["CityA", "2", "1", "736", "356438"], ["CityC", "3", "2", "1139", "473705"], ["CityC", "2", "2", "1074", "458227"], ["CityC", "2", "1", "652", "236328"], ["CityC", "2", "2", "908", "367640"], ["CityC", "3", "1", "1065", "425669"], ["CityA", "4", "1", "1110", "587941"]]
          }, {
            "name": "DataFrame Size",
            "description": "DataFrame Size. Number of columns and number of rows in the DataFrame.",
            "columnNames": ["Number of columns", "Number of rows"],
            "columnTypes": ["numeric", "numeric"],
            "rowNames": null,
            "values": [["5", "999"]]
          }],
          "distributions": {
            "city": {
              "name": "city",
              "missingValues": 0,
              "subtype": "discrete",
              "description": "Discrete distribution for city column",
              "buckets": ["CityA", "CityB", "CityC"],
              "counts": [327, 329, 343]
            },
            "price": {
              "name": "price",
              "missingValues": 0,
              "subtype": "continuous",
              "statistics": {
                "max": "816408",
                "min": "221661",
                "mean": "471568.263263"
              },
              "description": "Continuous distribution for price column",
              "buckets": ["221661", "251398.35", "281135.7", "310873.05", "340610.4", "370347.75", "400085.1", "429822.45", "459559.8", "489297.15", "519034.5", "548771.85", "578509.2", "608246.55", "637983.9", "667721.25", "697458.6", "727195.95", "756933.3", "786670.65", "816408"],
              "counts": [10, 31, 23, 53, 82, 79, 102, 110, 99, 81, 91, 69, 46, 36, 34, 22, 12, 6, 9, 4]
            },
            "sq_ft": {
              "name": "sq_ft",
              "missingValues": 0,
              "subtype": "continuous",
              "statistics": {
                "max": "1498",
                "min": "600",
                "mean": "1043.002002"
              },
              "description": "Continuous distribution for sq_ft column",
              "buckets": ["600", "644.9", "689.8", "734.7", "779.6", "824.5", "869.4", "914.3", "959.2", "1004.1", "1049", "1093.9", "1138.8", "1183.7", "1228.6", "1273.5", "1318.4", "1363.3", "1408.2", "1453.1", "1498"],
              "counts": [31, 25, 15, 26, 59, 78, 64, 64, 59, 97, 90, 58, 45, 72, 64, 59, 27, 25, 19, 22]
            },
            "baths": {
              "name": "baths",
              "missingValues": 0,
              "subtype": "continuous",
              "statistics": {
                "max": "2",
                "min": "1",
                "mean": "1.478478"
              },
              "description": "Continuous distribution for baths column",
              "buckets": ["1", "1.05", "1.1", "1.15", "1.2", "1.25", "1.3", "1.35", "1.4", "1.45", "1.5", "1.55", "1.6", "1.65", "1.7", "1.75", "1.8", "1.85", "1.9", "1.95", "2"],
              "counts": [521, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 478]
            },
            "beds": {
              "name": "beds",
              "missingValues": 0,
              "subtype": "continuous",
              "statistics": {
                "max": "4",
                "min": "2",
                "mean": "2.984985"
              },
              "description": "Continuous distribution for beds column",
              "buckets": ["2", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9", "3", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9", "4"],
              "counts": [336, 0, 0, 0, 0, 0, 0, 0, 0, 0, 342, 0, 0, 0, 0, 0, 0, 0, 0, 321]
            }
          }
        }
      },
      "270b15f0-c208-41b9-9031-6f75187c3eb8": {
        "className": "io.deepsense.deeplang.doperables.dataframe.DataFrame",
        "report": {
          "name": "DataFrame Report",
          "reportType": "DataFrameFull",
          "tables": [{
            "name": "Data Sample",
            "description": "Data Sample. Randomly selected 10 rows",
            "columnNames": ["sq_ft", "price"],
            "columnTypes": ["numeric", "numeric"],
            "rowNames": null,
            "values": [["820", "449178"], ["656", "267975"], ["636", "348946"], ["736", "356438"], ["1139", "473705"], ["1074", "458227"], ["652", "236328"], ["908", "367640"], ["1065", "425669"], ["1110", "587941"]]
          }, {
            "name": "DataFrame Size",
            "description": "DataFrame Size. Number of columns and number of rows in the DataFrame.",
            "columnNames": ["Number of columns", "Number of rows"],
            "columnTypes": ["numeric", "numeric"],
            "rowNames": null,
            "values": [["2", "999"]]
          }],
          "distributions": {
            "sq_ft": {
              "name": "sq_ft",
              "missingValues": 0,
              "subtype": "continuous",
              "statistics": {
                "max": "1498",
                "min": "600",
                "mean": "1043.002002"
              },
              "description": "Continuous distribution for sq_ft column",
              "buckets": ["600", "644.9", "689.8", "734.7", "779.6", "824.5", "869.4", "914.3", "959.2", "1004.1", "1049", "1093.9", "1138.8", "1183.7", "1228.6", "1273.5", "1318.4", "1363.3", "1408.2", "1453.1", "1498"],
              "counts": [31, 25, 15, 26, 59, 78, 64, 64, 59, 97, 90, 58, 45, 72, 64, 59, 27, 25, 19, 22]
            },
            "price": {
              "name": "price",
              "missingValues": 0,
              "subtype": "continuous",
              "statistics": {
                "max": "816408",
                "min": "221661",
                "mean": "471568.263263"
              },
              "description": "Continuous distribution for price column",
              "buckets": ["221661", "251398.35", "281135.7", "310873.05", "340610.4", "370347.75", "400085.1", "429822.45", "459559.8", "489297.15", "519034.5", "548771.85", "578509.2", "608246.55", "637983.9", "667721.25", "697458.6", "727195.95", "756933.3", "786670.65", "816408"],
              "counts": [10, 31, 23, 53, 82, 79, 102, 110, 99, 81, 91, 69, 46, 36, 34, 22, 12, 6, 9, 4]
            }
          }
        }
      },
      "74ed4653-b396-4e58-af32-92aeb29a7b19": {
        "className": "io.deepsense.deeplang.doperables.ColumnsFilterer",
        "report": {
          "name": "empty report",
          "reportType": "Empty",
          "tables": [],
          "distributions": {

          }
        }
      }
    },
    "nodes": {
      "051259cb-189f-2706-6f5a-7f684d52c849": {
        "ended": "2016-01-04T11:58:56.404Z",
        "results": ["04c20743-a380-42f3-bfa9-7ef648414fb6"],
        "error": null,
        "status": "COMPLETED",
        "started": "2016-01-04T11:58:50.508Z"
      },
      "bf6900e4-eea1-8f6c-6a0e-8d79fd2bb165": {
        "ended": "2016-01-04T11:58:57.191Z",
        "results": null,
        "error": {
          "code": "NodeFailure",
          "id": "ea50c0c5-f877-4cc1-b071-0cc4398d17f3",
          "details": {
            "stacktrace": "Job aborted due to stage failure: Task 1 in stage 8.0 failed 1 times, most recent failure: Lost task 1.0 in stage 8.0 (TID 14, localhost): java.io.IOException: Mkdirs failed to create file:/root/filtered_transactions.csv/_temporary/0/_temporary/attempt_201601041258_0008_m_000001_14 (exists=false, cwd=file:/home/gzes/newWorkflow)\n\tat org.apache.hadoop.fs.ChecksumFileSystem.create(ChecksumFileSystem.java:442)\n\tat org.apache.hadoop.fs.ChecksumFileSystem.create(ChecksumFileSystem.java:428)\n\tat org.apache.hadoop.fs.FileSystem.create(FileSystem.java:908)\n\tat org.apache.hadoop.fs.FileSystem.create(FileSystem.java:801)\n\tat org.apache.hadoop.mapred.TextOutputFormat.getRecordWriter(TextOutputFormat.java:123)\n\tat org.apache.spark.SparkHadoopWriter.open(SparkHadoopWriter.scala:91)\n\tat org.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopDataset$1$$anonfun$13.apply(PairRDDFunctions.scala:1104)\n\tat org.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopDataset$1$$anonfun$13.apply(PairRDDFunctions.scala:1095)\n\tat org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:66)\n\tat org.apache.spark.scheduler.Task.run(Task.scala:88)\n\tat org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:214)\n\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)\n\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)\n\tat java.lang.Thread.run(Thread.java:745)\n\nDriver stacktrace:\n\norg.apache.spark.scheduler.DAGScheduler.org$apache$spark$scheduler$DAGScheduler$$failJobAndIndependentStages(DAGScheduler.scala:1283)\norg.apache.spark.scheduler.DAGScheduler$$anonfun$abortStage$1.apply(DAGScheduler.scala:1271)\norg.apache.spark.scheduler.DAGScheduler$$anonfun$abortStage$1.apply(DAGScheduler.scala:1270)\nscala.collection.mutable.ResizableArray$class.foreach(ResizableArray.scala:59)\nscala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:47)\norg.apache.spark.scheduler.DAGScheduler.abortStage(DAGScheduler.scala:1270)\norg.apache.spark.scheduler.DAGScheduler$$anonfun$handleTaskSetFailed$1.apply(DAGScheduler.scala:697)\norg.apache.spark.scheduler.DAGScheduler$$anonfun$handleTaskSetFailed$1.apply(DAGScheduler.scala:697)\nscala.Option.foreach(Option.scala:236)\norg.apache.spark.scheduler.DAGScheduler.handleTaskSetFailed(DAGScheduler.scala:697)\norg.apache.spark.scheduler.DAGSchedulerEventProcessLoop.doOnReceive(DAGScheduler.scala:1496)\norg.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:1458)\norg.apache.spark.scheduler.DAGSchedulerEventProcessLoop.onReceive(DAGScheduler.scala:1447)\norg.apache.spark.util.EventLoop$$anon$1.run(EventLoop.scala:48)\norg.apache.spark.scheduler.DAGScheduler.runJob(DAGScheduler.scala:567)\norg.apache.spark.SparkContext.runJob(SparkContext.scala:1824)\norg.apache.spark.SparkContext.runJob(SparkContext.scala:1837)\norg.apache.spark.SparkContext.runJob(SparkContext.scala:1914)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopDataset$1.apply$mcV$sp(PairRDDFunctions.scala:1124)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopDataset$1.apply(PairRDDFunctions.scala:1065)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopDataset$1.apply(PairRDDFunctions.scala:1065)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:147)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:108)\norg.apache.spark.rdd.RDD.withScope(RDD.scala:310)\norg.apache.spark.rdd.PairRDDFunctions.saveAsHadoopDataset(PairRDDFunctions.scala:1065)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopFile$4.apply$mcV$sp(PairRDDFunctions.scala:989)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopFile$4.apply(PairRDDFunctions.scala:965)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopFile$4.apply(PairRDDFunctions.scala:965)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:147)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:108)\norg.apache.spark.rdd.RDD.withScope(RDD.scala:310)\norg.apache.spark.rdd.PairRDDFunctions.saveAsHadoopFile(PairRDDFunctions.scala:965)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopFile$1.apply$mcV$sp(PairRDDFunctions.scala:897)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopFile$1.apply(PairRDDFunctions.scala:897)\norg.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsHadoopFile$1.apply(PairRDDFunctions.scala:897)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:147)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:108)\norg.apache.spark.rdd.RDD.withScope(RDD.scala:310)\norg.apache.spark.rdd.PairRDDFunctions.saveAsHadoopFile(PairRDDFunctions.scala:896)\norg.apache.spark.rdd.RDD$$anonfun$saveAsTextFile$1.apply$mcV$sp(RDD.scala:1430)\norg.apache.spark.rdd.RDD$$anonfun$saveAsTextFile$1.apply(RDD.scala:1409)\norg.apache.spark.rdd.RDD$$anonfun$saveAsTextFile$1.apply(RDD.scala:1409)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:147)\norg.apache.spark.rdd.RDDOperationScope$.withScope(RDDOperationScope.scala:108)\norg.apache.spark.rdd.RDD.withScope(RDD.scala:310)\norg.apache.spark.rdd.RDD.saveAsTextFile(RDD.scala:1409)\ncom.databricks.spark.csv.package$CsvSchemaRDD.saveAsCsvFile(package.scala:169)\ncom.databricks.spark.csv.DefaultSource.createRelation(DefaultSource.scala:165)\norg.apache.spark.sql.execution.datasources.ResolvedDataSource$.apply(ResolvedDataSource.scala:170)\norg.apache.spark.sql.DataFrameWriter.save(DataFrameWriter.scala:146)\norg.apache.spark.sql.DataFrameWriter.save(DataFrameWriter.scala:137)\nio.deepsense.deeplang.doperations.WriteDataFrame.writeToFile(WriteDataFrame.scala:122)\nio.deepsense.deeplang.doperations.WriteDataFrame._execute(WriteDataFrame.scala:64)\nio.deepsense.deeplang.doperations.WriteDataFrame._execute(WriteDataFrame.scala:38)\nio.deepsense.deeplang.DOperation1To0.execute(DOperations.scala:242)\nio.deepsense.workflowexecutor.WorkflowNodeExecutorActor.executeOperation(WorkflowNodeExecutorActor.scala:112)\nio.deepsense.workflowexecutor.WorkflowNodeExecutorActor$$anonfun$receive$1.applyOrElse(WorkflowNodeExecutorActor.scala:52)\nakka.actor.Actor$class.aroundReceive(Actor.scala:467)\nio.deepsense.workflowexecutor.WorkflowNodeExecutorActor.aroundReceive(WorkflowNodeExecutorActor.scala:34)\nakka.actor.ActorCell.receiveMessage(ActorCell.scala:516)\nakka.actor.ActorCell.invoke(ActorCell.scala:487)\nakka.dispatch.Mailbox.processMailbox(Mailbox.scala:238)\nakka.dispatch.Mailbox.run(Mailbox.scala:220)\nakka.dispatch.ForkJoinExecutorConfigurator$AkkaForkJoinTask.exec(AbstractDispatcher.scala:397)\nscala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)\nscala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)\nscala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)\nscala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)\n"
          },
          "message": "Unable to write file: file:///root/filtered_transactions.csv",
          "title": "WriteFileException"
        },
        "status": "FAILED",
        "started": "2016-01-04T11:58:56.860Z"
      },
      "67610468-7121-a364-ac17-68d578dec03b": {
        "ended": "2016-01-04T11:58:56.860Z",
        "results": ["270b15f0-c208-41b9-9031-6f75187c3eb8", "74ed4653-b396-4e58-af32-92aeb29a7b19"],
        "error": null,
        "status": "COMPLETED",
        "started": "2016-01-04T11:58:56.406Z"
      }
    },
    "error": null
  }
}
{% endhighlight %}
</div>
