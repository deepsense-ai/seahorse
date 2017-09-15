/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 08.06.15.
 */

'use strict';

angular.module('test', ['deepsense.navigation-panel']).controller('DemoCrtl', [
  '$scope', function ($scope) {
    $scope.nodeCollection = {
      "8f7de13d-9daf-d929-b0ff-03507d12aaa5": {
        "id": "8f7de13d-9daf-d929-b0ff-03507d12aaa5",
        "description": "Joins two DataFrames to a DataFrame",
        "name": "Join",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "06374446-3138-4cf7-9682-f884990f3a60",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "8f7de13d-9daf-d929-b0ff-03507d12aaa5",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-8f7de13d-9daf-d929-b0ff-03507d12aaa5"
          },
          {
            "nodeId": "8f7de13d-9daf-d929-b0ff-03507d12aaa5",
            "index": 1,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-1-8f7de13d-9daf-d929-b0ff-03507d12aaa5"
          }
        ],
        "output": [
          {
            "nodeId": "8f7de13d-9daf-d929-b0ff-03507d12aaa5",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-8f7de13d-9daf-d929-b0ff-03507d12aaa5"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "8f7de13d-9daf-d929-b0ff-03507d12aaa5",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-8f7de13d-9daf-d929-b0ff-03507d12aaa5"
          }
        ],
        "edges": {},
        "x": 2581,
        "y": 3544,
        "parametersValues": {},
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Nothing is connected to the port 1",
          "Parameter JOIN COLUMNS is required but was not provided."
        ]
      },
      "e084d629-7962-2f07-4067-e3325d333c6b": {
        "id": "e084d629-7962-2f07-4067-e3325d333c6b",
        "description": "Executes an SQL expression on a DataFrame",
        "name": "SQL Expression",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "530e1420-7fbe-416b-b685-6c1e0f1137fc",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "e084d629-7962-2f07-4067-e3325d333c6b",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-e084d629-7962-2f07-4067-e3325d333c6b"
          }
        ],
        "output": [
          {
            "nodeId": "e084d629-7962-2f07-4067-e3325d333c6b",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-e084d629-7962-2f07-4067-e3325d333c6b"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "e084d629-7962-2f07-4067-e3325d333c6b",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-e084d629-7962-2f07-4067-e3325d333c6b"
          }
        ],
        "edges": {},
        "x": 3129,
        "y": 3828,
        "parametersValues": {},
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Parameter DATAFRAME ID is required but was not provided."
        ]
      },
      "ad30bec6-4cb9-c1fa-ca87-255f408a8194": {
        "id": "ad30bec6-4cb9-c1fa-ca87-255f408a8194",
        "description": "Joins two DataFrames to a DataFrame",
        "name": "Join",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "06374446-3138-4cf7-9682-f884990f3a60",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "ad30bec6-4cb9-c1fa-ca87-255f408a8194",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-ad30bec6-4cb9-c1fa-ca87-255f408a8194"
          },
          {
            "nodeId": "ad30bec6-4cb9-c1fa-ca87-255f408a8194",
            "index": 1,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-1-ad30bec6-4cb9-c1fa-ca87-255f408a8194"
          }
        ],
        "output": [
          {
            "nodeId": "ad30bec6-4cb9-c1fa-ca87-255f408a8194",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-ad30bec6-4cb9-c1fa-ca87-255f408a8194"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "ad30bec6-4cb9-c1fa-ca87-255f408a8194",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-ad30bec6-4cb9-c1fa-ca87-255f408a8194"
          }
        ],
        "edges": {},
        "x": 2690,
        "y": 3684,
        "parametersValues": {},
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Nothing is connected to the port 1",
          "Parameter JOIN COLUMNS is required but was not provided."
        ]
      },
      "9c621836-563a-ef95-c7be-098ddf8acbec": {
        "id": "9c621836-563a-ef95-c7be-098ddf8acbec",
        "description": "Converts selected columns of a DataFrame to a different type",
        "name": "Convert Type",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "f8b3c5d0-febe-11e4-b939-0800200c9a66",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "9c621836-563a-ef95-c7be-098ddf8acbec",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-9c621836-563a-ef95-c7be-098ddf8acbec"
          }
        ],
        "output": [
          {
            "nodeId": "9c621836-563a-ef95-c7be-098ddf8acbec",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-9c621836-563a-ef95-c7be-098ddf8acbec"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "9c621836-563a-ef95-c7be-098ddf8acbec",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-9c621836-563a-ef95-c7be-098ddf8acbec"
          }
        ],
        "edges": {},
        "x": 2978,
        "y": 3602,
        "parametersValues": null,
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Parameter SELECTED COLUMNS is required but was not provided."
        ],
        "parameters": {
          "parameters": [
            {
              "factoryItem": {},
              "name": "selected columns",
              "items": [],
              "schema": {
                "name": "selected columns",
                "description": "Columns to be converted",
                "isSingle": false,
                "portIndex": 0,
                "type": "selector",
                "required": true
              },
              "excluding": false
            },
            {
              "name": "target type",
              "schema": {
                "name": "target type",
                "description": "Target type of the columns",
                "values": [
                  {
                    "name": "numeric",
                    "schema": null
                  },
                  {
                    "name": "string",
                    "schema": null
                  },
                  {
                    "name": "categorical",
                    "schema": null
                  }
                ],
                "type": "choice",
                "required": true
              },
              "possibleChoicesList": {
                "numeric": {
                  "parameters": []
                },
                "string": {
                  "parameters": []
                },
                "categorical": {
                  "parameters": []
                }
              },
              "choices": {
                "numeric": false,
                "string": false,
                "categorical": false
              }
            }
          ]
        }
      },
      "24abc816-078a-3058-d7e3-e2d637581638": {
        "id": "24abc816-078a-3058-d7e3-e2d637581638",
        "description": "Creates a new DataFrame containing all rows from both input DataFrames",
        "name": "Union",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "90fed07b-d0a9-49fd-ae23-dd7000a1d8ad",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "24abc816-078a-3058-d7e3-e2d637581638",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-24abc816-078a-3058-d7e3-e2d637581638"
          },
          {
            "nodeId": "24abc816-078a-3058-d7e3-e2d637581638",
            "index": 1,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-1-24abc816-078a-3058-d7e3-e2d637581638"
          }
        ],
        "output": [
          {
            "nodeId": "24abc816-078a-3058-d7e3-e2d637581638",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-24abc816-078a-3058-d7e3-e2d637581638"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "24abc816-078a-3058-d7e3-e2d637581638",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-24abc816-078a-3058-d7e3-e2d637581638"
          }
        ],
        "edges": {},
        "x": 2892,
        "y": 3423,
        "parametersValues": {},
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Nothing is connected to the port 1"
        ]
      },
      "239fd6c9-4a2a-9523-bb71-9995ef976722": {
        "id": "239fd6c9-4a2a-9523-bb71-9995ef976722",
        "description": "Applies a Transformation to a DataFrame",
        "name": "Apply Transformation",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "f6e1f59b-d04d-44e2-ae35-2fcada44d23f",
        "icon": "fa-bolt",
        "input": [
          {
            "nodeId": "239fd6c9-4a2a-9523-bb71-9995ef976722",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.Transformation"
            ],
            "id": "input-0-239fd6c9-4a2a-9523-bb71-9995ef976722"
          },
          {
            "nodeId": "239fd6c9-4a2a-9523-bb71-9995ef976722",
            "index": 1,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-1-239fd6c9-4a2a-9523-bb71-9995ef976722"
          }
        ],
        "output": [
          {
            "nodeId": "239fd6c9-4a2a-9523-bb71-9995ef976722",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-239fd6c9-4a2a-9523-bb71-9995ef976722"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "239fd6c9-4a2a-9523-bb71-9995ef976722",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-239fd6c9-4a2a-9523-bb71-9995ef976722"
          }
        ],
        "edges": {},
        "x": 1351,
        "y": 4271,
        "parametersValues": null,
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Nothing is connected to the port 1"
        ],
        "parameters": {
          "parameters": []
        }
      },
      "85fe27d9-9a1b-a490-4045-a8ec2f166ded": {
        "id": "85fe27d9-9a1b-a490-4045-a8ec2f166ded",
        "description": "Extracts Numeric fields (year, month, etc.) from a Timestamp",
        "name": "Decompose Datetime",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "42f2eb12-e28b-11e4-8a00-1681e6b88ec1",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "85fe27d9-9a1b-a490-4045-a8ec2f166ded",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-85fe27d9-9a1b-a490-4045-a8ec2f166ded"
          }
        ],
        "output": [
          {
            "nodeId": "85fe27d9-9a1b-a490-4045-a8ec2f166ded",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-85fe27d9-9a1b-a490-4045-a8ec2f166ded"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "85fe27d9-9a1b-a490-4045-a8ec2f166ded",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-85fe27d9-9a1b-a490-4045-a8ec2f166ded"
          }
        ],
        "edges": {},
        "x": 1529,
        "y": 4076,
        "parametersValues": {},
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Parameter TIMESTAMP COLUMN is required but was not provided."
        ]
      },
      "f2a57ddd-c5b7-2142-8cc1-4a147351c72f": {
        "id": "f2a57ddd-c5b7-2142-8cc1-4a147351c72f",
        "description": "Joins two DataFrames to a DataFrame",
        "name": "Join",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "06374446-3138-4cf7-9682-f884990f3a60",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "f2a57ddd-c5b7-2142-8cc1-4a147351c72f",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-f2a57ddd-c5b7-2142-8cc1-4a147351c72f"
          },
          {
            "nodeId": "f2a57ddd-c5b7-2142-8cc1-4a147351c72f",
            "index": 1,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-1-f2a57ddd-c5b7-2142-8cc1-4a147351c72f"
          }
        ],
        "output": [
          {
            "nodeId": "f2a57ddd-c5b7-2142-8cc1-4a147351c72f",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-f2a57ddd-c5b7-2142-8cc1-4a147351c72f"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "f2a57ddd-c5b7-2142-8cc1-4a147351c72f",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-f2a57ddd-c5b7-2142-8cc1-4a147351c72f"
          }
        ],
        "edges": {},
        "x": 1681,
        "y": 4347,
        "parametersValues": {},
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Nothing is connected to the port 1",
          "Parameter JOIN COLUMNS is required but was not provided."
        ]
      },
      "b8912296-133b-6ede-5cae-8544f4b7554b": {
        "id": "b8912296-133b-6ede-5cae-8544f4b7554b",
        "description": "Creates a Transformation that creates a new column based on a mathematical formula",
        "name": "Create Mathematical Transformation",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "ecb9bc36-5f7c-4a62-aa18-8db6e2d73251",
        "icon": "fa-bolt",
        "input": [],
        "output": [
          {
            "nodeId": "b8912296-133b-6ede-5cae-8544f4b7554b",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.transformations.MathematicalTransformation"
            ],
            "id": "output-0-b8912296-133b-6ede-5cae-8544f4b7554b"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "b8912296-133b-6ede-5cae-8544f4b7554b",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.transformations.MathematicalTransformation"
            ],
            "id": "output-0-b8912296-133b-6ede-5cae-8544f4b7554b"
          }
        ],
        "edges": {},
        "x": 1495,
        "y": 3585,
        "parametersValues": {},
        "state": null,
        "knowledgeErrors": [
          "Parameter FORMULA is required but was not provided."
        ]
      },
      "8cfe1b27-7b8c-65a0-8cbd-432bc53279f6": {
        "id": "8cfe1b27-7b8c-65a0-8cbd-432bc53279f6",
        "description": "One-hot encodes categorical columns of a DataFrame",
        "name": "One Hot Encoder",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "b1b6eefe-f7b7-11e4-a322-1697f925ec7b",
        "icon": "fa-pencil-square-o",
        "input": [
          {
            "nodeId": "8cfe1b27-7b8c-65a0-8cbd-432bc53279f6",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-8cfe1b27-7b8c-65a0-8cbd-432bc53279f6"
          }
        ],
        "output": [
          {
            "nodeId": "8cfe1b27-7b8c-65a0-8cbd-432bc53279f6",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-8cfe1b27-7b8c-65a0-8cbd-432bc53279f6"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "8cfe1b27-7b8c-65a0-8cbd-432bc53279f6",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-8cfe1b27-7b8c-65a0-8cbd-432bc53279f6"
          }
        ],
        "edges": {},
        "x": 2071,
        "y": 3732,
        "parametersValues": null,
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Parameter COLUMNS is required but was not provided."
        ],
        "parameters": {
          "parameters": [
            {
              "factoryItem": {},
              "name": "columns",
              "items": [],
              "schema": {
                "name": "columns",
                "description": "Categorical columns to encode",
                "isSingle": false,
                "portIndex": 0,
                "type": "selector",
                "required": true
              },
              "excluding": false
            },
            {
              "name": "with redundant",
              "value": false,
              "schema": {
                "name": "with redundant",
                "description": "Preserve redundant column",
                "default": false,
                "type": "boolean",
                "required": true
              }
            },
            {
              "name": "prefix",
              "schema": {
                "type": "prefixBasedCreator",
                "description": "Prefix for generated columns",
                "required": false,
                "name": "prefix"
              }
            }
          ]
        }
      },
      "9003ead8-206e-43e0-732c-9794b9473035": {
        "id": "9003ead8-206e-43e0-732c-9794b9473035",
        "description": "Trains Normalizer on a DataFrame",
        "name": "Train Normalizer",
        "uiName": "",
        "color": "#00B1EB",
        "operationId": "9ae1a3f8-a70b-44bf-b162-e3b16dc6fded",
        "icon": "fa-bolt",
        "input": [
          {
            "nodeId": "9003ead8-206e-43e0-732c-9794b9473035",
            "index": 0,
            "type": "input",
            "required": true,
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "input-0-9003ead8-206e-43e0-732c-9794b9473035"
          }
        ],
        "output": [
          {
            "nodeId": "9003ead8-206e-43e0-732c-9794b9473035",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-9003ead8-206e-43e0-732c-9794b9473035"
          },
          {
            "nodeId": "9003ead8-206e-43e0-732c-9794b9473035",
            "index": 1,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.Normalizer"
            ],
            "id": "output-1-9003ead8-206e-43e0-732c-9794b9473035"
          }
        ],
        "originalOutput": [
          {
            "nodeId": "9003ead8-206e-43e0-732c-9794b9473035",
            "index": 0,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.dataframe.DataFrame"
            ],
            "id": "output-0-9003ead8-206e-43e0-732c-9794b9473035"
          },
          {
            "nodeId": "9003ead8-206e-43e0-732c-9794b9473035",
            "index": 1,
            "type": "output",
            "typeQualifier": [
              "io.deepsense.deeplang.doperables.Normalizer"
            ],
            "id": "output-1-9003ead8-206e-43e0-732c-9794b9473035"
          }
        ],
        "edges": {},
        "x": 2033,
        "y": 3503,
        "parametersValues": null,
        "state": null,
        "knowledgeErrors": [
          "Nothing is connected to the port 0",
          "Parameter SELECTED COLUMNS is required but was not provided."
        ],
        "parameters": {
          "parameters": [
            {
              "factoryItem": {},
              "name": "selected columns",
              "items": [],
              "schema": {
                "name": "selected columns",
                "description": "Columns that the normalizer should be able to normalize",
                "isSingle": false,
                "portIndex": 0,
                "type": "selector",
                "required": true
              },
              "excluding": false
            }
          ]
        }
      }
    };
  }
]);