---
layout: documentation
displayTitle: Workflow File
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
[workflow](deeplang.html#workflows) design and results
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

Metadata contains information about [workflow](deeplang.html#workflows)
type and Seahorse API version.

| Property | Description |
| --- | --- |
| `type` | Execution mode for the workflow: "batch" or "streaming". Currently only batch workflows are supported. |
| `apiVersion` |  Seahorse API version. |

##### Example
{:.no_toc}

{% highlight json %}
"metadata": {
  "type": "batch",
  "apiVersion": "{{ site.WORKFLOW_EXECUTOR_VERSION }}"
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
	"id": "83bad450-f87c-11e4-b939-0800200c9a66",
	"name": "File To DataFrame",
	"version": "0.1.0"
      },
      "parameters": {
	"format": {
	  "CSV": {
	    "separator": ",",
	    "names included": true
	  }
	},
	"categorical columns": [
	  {
	    "type": "columnList",
	    "values": []
	  }
	]
      }
    },
    {
      "id": "c20f5b58-4193-11e5-a151-feff819cdc9f",
      "operation": {
	"id": "d273c42f-b840-4402-ba6b-18282cc68de3",
	"name": "Split DataFrame",
	"version": "0.1.0"
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

Execution report contains information about [workflow](deeplang.html#workflows)
execution on the cluster: status information, errors, entity reports.
This section is optional.

| Property | Description |
| --- | --- |
| ``status`` | Overall execution status - ``COMPLETED``, ``FAILED`` |
| ``error`` | Error message - present only if the workflow execution failed. |
| ``started`` | Timestamp of workflow execution start. It contains date, time and timezone according to ISO 8601 standard.<br>Example: ``2015-05-12T21:11:09Z`` |
| ``ended`` | Timestamp of workflow execution finish. Format is the same as in ``started``. |
| ``nodes`` | Described in following [Nodes](#nodes-1) subsection. |
| ``resultEntities`` | Described in following [Result entities](#result-entities) subsection. |

#### Nodes

Subsection *nodes* contains information about [operation](operations.html) execution status.

| Property | Description |
| --- | --- |
| ``status`` | Operation execution status - ``COMPLETED`` or ``FAILED``. |
| ``error`` | Error message - present only if operation execution failed. |
| ``started`` | Timestamp of operation execution start. Format is the same as in [execution report](#execution-report). |
| ``ended`` | Timestamp of operation execution finish. Format is the same as in [execution report](#execution-report). |
| ``results`` | UUID identifiers of output entities produced by the operation. |

#### Result Entities

Subsection *resultEntities* contains information about entities created as a result of
[workflow](deeplang.html#workflows) execution, such as data frames and models.

| Property | Description |
| --- | --- |
| ``className`` | Class name of described entity. |
| ``report`` | Object containing detailed entity report about each entity. The exact form is dependent on the entity type. |

##### Example
{:.no_toc}

{% highlight json %}
"executionReport": {
  "status": "COMPLETED",
  "error": null,
  "started": "2015-05-12T21:11:09Z",
  "ended": "2015-05-14T12:03:55Z",
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
by the [Workflow Executor](workflowexecutor.html).

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
  "id": "bbf34cbb-9ee6-442f-80a8-22886b860933",
  "metadata": {
    "type": "batch",
    "apiVersion": "0.9.0"
  },
  "workflow": {
    "nodes": [
      {
        "id": "742743c3-a0b1-41dc-82b1-172d68e1e814",
        "operation": {
          "id": "71a45d0f-949e-439f-bfae-2e905e160a4c",
          "name": "Read DataFrame"
        },
        "parameters": {
          "path": "hdfs://resources/dataframe1",
          "data source": "json"
        }
      },
      {
        "id": "6a27687a-dd30-4245-86bc-7cf4db670eab",
        "operation": {
          "id": "d273c42f-b840-4402-ba6b-18282cc68de3",
          "name": "Split DataFrame",
          "version": "0.1.0"
        },
        "parameters": {
          "split ratio": 0.7,
          "seed": 1.0
        }
      },
      {
        "id": "ad3c82f3-3afa-4fd6-b1ee-075180914592",
          "operation": {
            "id": "0643f308-f2fa-11e4-b9b2-1697f925ec7b",
            "name": "Create Ridge Regression",
            "version": "0.1.0"
          },
          "parameters": {
            "regularization": 0.5,
            "iterations number": 1.0
          }
      },
      {
        "id": "3d55ea34-1897-4128-8025-90c73ed69a4d",
        "operation": {
          "id": "c526714c-e7fb-11e4-b02c-1681e6b88ec1",
          "name": "Train regressor",
          "version": "0.1.0"
        },
        "parameters": {
          "feature columns": [
            {
              "type": "typeList",
              "values": ["numeric"]
            },
            {
              "type": "nameList",
              "value": ["rating"],
              "exclude": true
            }
          ],
          "target column": {
            "type": "name",
            "value": "rating"
          }
        }
      },
      {
        "id": "d2dd9574-1a76-4c87-ae4e-aba0b959a0cc",
        "operation": {
          "id": "6cf6867c-e7fd-11e4-b02c-1681e6b88ec1",
          "name": "Score regressor"
        },
        "parameters": {
          "prediction column": "rating_prediction"
        }
      },
      {
        "id": "c3cf202c-9035-4e11-851c-7c4869be0ed3",
        "operation": {
          "id": "f2a43e21-331e-42d3-8c02-7db1da20bc00",
          "name": "Evaluate Regression"
        },
        "parameters": {
          "target column": {
            "type": "column",
            "value": "rating"
          },
          "prediction column": {
            "type": "column",
            "value": "rating_prediction"
          }
        }
      },
      {
        "id": "2407ebc5-916d-4e4f-b1ff-4061ebb753b4",
        "operation": {
          "id": "ee585aba-1067-4262-ae44-881ef67e8486",
          "name": "Write DataFrame"
        },
        "parameters": {
          "path": "hdfs://resources/dataframe2",
          "data source": "json"
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
          "nodeId": "6a27687a-dd30-4245-86bc-7cf4db670eab",
          "portIndex": 0
        }
      },
      {
        "from": {
          "nodeId": "6a27687a-dd30-4245-86bc-7cf4db670eab",
          "portIndex": 0
        },
        "to": {
          "nodeId": "3d55ea34-1897-4128-8025-90c73ed69a4d",
          "portIndex": 1
        }
      },
      {
        "from": {
          "nodeId": "6a27687a-dd30-4245-86bc-7cf4db670eab",
          "portIndex": 1
        },
        "to": {
          "nodeId": "d2dd9574-1a76-4c87-ae4e-aba0b959a0cc",
          "portIndex": 1
        }
      },
      {
        "from": {
          "nodeId": "ad3c82f3-3afa-4fd6-b1ee-075180914592",
          "portIndex": 0
        },
        "to": {
          "nodeId": "3d55ea34-1897-4128-8025-90c73ed69a4d",
          "portIndex": 0
        }
      },
      {
        "from": {
          "nodeId": "3d55ea34-1897-4128-8025-90c73ed69a4d",
          "portIndex": 0
        },
        "to": {
          "nodeId": "d2dd9574-1a76-4c87-ae4e-aba0b959a0cc",
          "portIndex": 0
        }
      },
      {
        "from": {
          "nodeId": "d2dd9574-1a76-4c87-ae4e-aba0b959a0cc",
          "portIndex": 0
        },
        "to": {
          "nodeId": "c3cf202c-9035-4e11-851c-7c4869be0ed3",
          "portIndex": 0
        }
      },
      {
        "from": {
          "nodeId": "d2dd9574-1a76-4c87-ae4e-aba0b959a0cc",
          "portIndex": 0
        },
        "to": {
          "nodeId": "2407ebc5-916d-4e4f-b1ff-4061ebb753b4",
          "portIndex": 0
        }
      }
    ]
  },
  "thirdPartyData": {
    "seahorseEditor": {
    "name": "My first workflow",
    "description": "An example of a workflow.",
      "nodes": {
        "742743c3-a0b1-41dc-82b1-172d68e1e814": {
          "coordinates": {
            "x": 123,
            "y": 419
          }
        },
        "6a27687a-dd30-4245-86bc-7cf4db670eab": {
          "coordinates": {
            "x": 500,
            "y": 329
          }
        },
        "ad3c82f3-3afa-4fd6-b1ee-075180914592": {
          "coordinates": {
            "x": 245,
            "y": 345
          }
        },
        "3d55ea34-1897-4128-8025-90c73ed69a4d": {
          "coordinates": {
            "x": 122,
            "y": 879
          }
        },
        "d2dd9574-1a76-4c87-ae4e-aba0b959a0cc": {
          "coordinates": {
            "x": 123,
            "y": 876
          }
        },
        "c3cf202c-9035-4e11-851c-7c4869be0ed3": {
          "coordinates": {
            "x": 424,
            "y": 767
          }
        },
        "2407ebc5-916d-4e4f-b1ff-4061ebb753b4": {
          "coordinates": {
            "x": 873,
            "y": 334
          }
        }
      }
    }
  },
  "executionReport": {
    "status": "FAILED",
    "error": "One of nodes failed",
    "started": "2015-06-12T21:11:09Z",
    "ended": "2015-06-12T21:15:55Z",
    "nodes": {
      "742743c3-a0b1-41dc-82b1-172d68e1e814": {
	"status": "COMPLETED",
	"started": "2015-06-12T21:11:09Z",
	"ended": "2015-05-14T12:03:55Z",
	"results": [
	  "2518d941-43cb-4288-b959-bc4441b86083"
	],
	"error": null
      },
      "6a27687a-dd30-4245-86bc-7cf4db670eab": {
	"status": "COMPLETED",
	"started": "2015-05-12T21:11:09Z",
	"ended": "2015-05-14T12:03:55Z",
	"results": [
	  "c376d234-b64f-4cec-8c47-6a3e54adf181",
	  "a63ea659-2f15-47dc-b133-b2a442fd9fce"
	],
	"error": null
      },
      "ad3c82f3-3afa-4fd6-b1ee-075180914592": {
	"status": "COMPLETED",
	"started": "2015-05-12T21:11:09Z",
	"ended": "2015-05-14T12:03:55Z",
	"results": [
	  "e36697f3-cd7b-41cc-bdf7-fefebd24ba5d"
	],
	"error": null
      },
      "3d55ea34-1897-4128-8025-90c73ed69a4d": {
	"status": "COMPLETED",
	"started": "2015-05-12T21:11:09Z",
	"ended": "2015-05-14T12:03:55Z",
	"results": [
	  "03b99144-3f50-452b-b4f9-67e5338383a7"
	],
	"error": null
      },
      "d2dd9574-1a76-4c87-ae4e-aba0b959a0cc": {
	"status": "COMPLETED",
	"started": "2015-05-12T21:11:09Z",
	"ended": "2015-05-14T12:03:55Z",
	"results": [
	  "da322b24-73b1-40c9-9ca9-84e847087a1f"
	],
	"error": null
      },
      "c3cf202c-9035-4e11-851c-7c4869be0ed3": {
	"status": "COMPLETED",
	"started": "2015-05-12T21:11:09Z",
	"ended": "2015-05-14T12:03:55Z",
	"results": [
	  "3b4151e4-2bfd-4aa5-a726-6cb5faf6caf2"
	],
	"error": null
      },
      "2407ebc5-916d-4e4f-b1ff-4061ebb753b4": {
	"status": "FAILED",
	"started": "2015-05-12T21:11:09Z",
	"ended": "2015-06-12T21:15:55Z",
	"results": [],
	"error": {
	  "message": "Could not write to specified path.",
	  "details": {
	    "stacktrace": "..."
	  }
	}
      }
    },
    "resultEntities": {
      "2518d941-43cb-4288-b959-bc4441b86083": {
        "className": "DataFrame",
        "report": null
      },
      "c376d234-b64f-4cec-8c47-6a3e54adf181": {
        "className": "DataFrame",
        "report": null
      },
      "a63ea659-2f15-47dc-b133-b2a442fd9fce": {
        "className": "DataFrame",
        "report": null
      },
      "e36697f3-cd7b-41cc-bdf7-fefebd24ba5d": {
        "className": "UntrainedRidgeRegression",
        "report": null
      },
      "03b99144-3f50-452b-b4f9-67e5338383a7": {
        "className": "TrainedRidgeRegression",
        "report": null
      },
      "da322b24-73b1-40c9-9ca9-84e847087a1f": {
        "className": "DataFrame",
        "report": null
      },
      "3b4151e4-2bfd-4aa5-a726-6cb5faf6caf2": {
        "className": "Report",
        "report": null
      }
    }
  }
}
{% endhighlight %}
</div>
