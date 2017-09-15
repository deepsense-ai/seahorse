/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Grzegorz Swatowski on 25.06.15.
 */

'use strict';

angular.module('test').
  factory('Model', ['DeepsenseNodeParameters', function (DeepsenseNodeParameters) {
    var nodes = [
      {
        description: 'Simple sql db reader',
        id: '88f560a9-97e3-4fa2-93ce-edad4cab7ea9',
        name: 'Sql Reader',
        operationId: '342342-123123-434234-234234',
        parameters: null
      },
      {
        description: 'Simple filters takes dataset based on...',
        id: '210ce781-0a9f-477d-99a7-5be31f7bfc02',
        name: 'Row Filter',
        operationId: '342342-123123-434234-12334',
        parameters: null
      },
      {
        description: 'Neural networks inspired by biological...',
        id: 'edc02822-b8ee-4225-b070-f892a87dbf11',
        name: 'Neural Network',
        operationId: '11111-111111-44444',
        parameters: null
      }
    ];

    var parameterSchemas = [
      [
        {
          "name": "connection_string",
          "type": "string",
          "description": "jdbc address",
          "default": "jdbc:",
          "required": true,
          "validator": {
            "type": "regex",
            "configuration": {
              "regex": "^jdbc:[a-z:/]*$"
            }
          }
        },
        {
          "name": "boolean_attr_test",
          "type": "boolean",
          "description": "boolean attr test",
          "default": true,
          "required": true
        },
        {
          "name": "sample_multiple_choice",
          "type": "multipleChoice",
          "description": "sample multiple choice description",
          "default": ["choice2-some-example-text"],
          "required": true,
          "values": [
            {
              "name": "choice1-long-text",
              "schema": [
                {
                  "name": "name_001",
                  "type": "numeric",
                  "description": "sample sum field",
                  "default": 101,
                  "required": false,
                  "validator": {
                    "type": "range",
                    "configuration": {
                      "begin": 100,
                      "end": 102,
                      "beginIncluded": false,
                      "endIncluded": true,
                      "step": 1
                    }
                  }
                }
              ]
            },
            {
              "name": "choice2-some-example-text",
              "schema": [
                {
                  "name": "name_002",
                  "type": "string",
                  "description": "string param",
                  "default": "2+2+2",
                  "required": true,
                  "validator": {
                    "type": "regex",
                    "configuration": {
                      "regex": "^\\d+[+-]\\d+[+=]\\d+$"
                    }
                  }
                }
              ]
            }
          ]
        },
        {
          "name": "sample-multiplier",
          "type": "multiplier",
          "description": "This is a multiplier parameter.",
          "required": true,
          "values": [
            {
              "name": "http-field",
              "type": "string",
              "description": "sample1",
              "default": "http://",
              "required": true,
              "validator": {
                "type": "regex",
                "configuration": {
                  "regex": "^http://"
                }
              }
            },
            {
              "name": "ftp-field",
              "type": "string",
              "description": "sample1",
              "default": "ftp://",
              "required": true,
              "validator": {
                "type": "regex",
                "configuration": {
                  "regex": "^ftp://"
                }
              }
            }
          ]
        }
      ],
      [
        {
          "name": "sample-selectors",
          "type": "selector",
          "description": "sample desc",
          "port": 1,
          "default": null,
          "required": true,
          "isSingle": false
        },
        {
          "name": "sample-single-index-selector",
          "type": "selector",
          "description": "sample desc",
          "port": 1,
          "default": null,
          "required": true,
          "isSingle": true
        },
        {
          "name": "sample-single-column-selector",
          "type": "selector",
          "description": "sample desc",
          "port": 1,
          "default": null,
          "required": true,
          "isSingle": true
        },
        {
          "name": "condition_threshold",
          "type": "numeric",
          "description": "condition threshold",
          "default": 6,
          "required": true,
          "validator": {
            "type": "range",
            "configuration": {
              "begin": 10.1,
              "end": 12.3,
              "beginIncluded": true,
              "endIncluded": true,
              "step": 1.1
            }
          }
        }
      ],
      [
        {
          "name": "sample_snippet",
          "type": "snippet",
          "description": "This is a snippet parameter.",
          "language": "R",
          "default": "m <- 1000",
          "required": true
        },
        {
          "name": "sample_choice",
          "type": "choice",
          "description": "sample choice description",
          "default": "choice1",
          "required": true,
          "values": [
            {
              "name": "choice1",
              "schema": [
                {
                  "name": "name11",
                  "type": "numeric",
                  "description": "11 desc",
                  "default": 42,
                  "required": true,
                  "validator": {
                    "type": "range",
                    "configuration": {
                      "begin": 40,
                      "end": 50,
                      "beginIncluded": true,
                      "endIncluded": false,
                      "step": 2
                    }
                  }
                },
                {
                  "name": "name21",
                  "type": "string",
                  "description": "21 desc",
                  "default": "word123",
                  "required": true,
                  "validator": {
                    "type": "regex",
                    "configuration": {
                      "regex": "^word[\\d]+$"
                    }
                  }
                }
              ]
            },
            {
              "name": "choice2",
              "schema": [
                {
                  "name": "name12",
                  "type": "boolean",
                  "description": "12 desc",
                  "default": true,
                  "required": false
                },
                {
                  "name": "name22",
                  "type": "snippet",
                  "description": "22 desc",
                  "language": "R",
                  "default": "exit 42;",
                  "required": true
                }
              ]
            },
            {
              "name": "choice3",
              "schema": [
                {
                  "name": "name31",
                  "type": "snippet",
                  "description": "31 desc",
                  "language": "R",
                  "default": "let codilime <- 42;",
                  "required": true
                }
              ]
            }
          ]
        }
      ]
    ];

    var parameterValues = [
      {
        "connection_string": "jdbc:test/string/field",
        "boolean_attr_test": false,
        "sample_multiple_choice": {
          "choice1-long-text": {
            "name_001": 102
          },
          "choice2-some-example-text": {
            "name_002": "2-2=0"
          },
          "sample-multiplier": [
            {
              "ftp-field": "ftp://www.test.tete/",
              "http-field": "http://test.te/"
            },
            {
              "ftp-field": "ftp://www.test210.tete/",
              "http-field": "http://test_2222.test/"
            }
          ]
        },
        "sample-multiplier": [
          {
            "http-field": "http://42.pl/",
            "ftp-field": "ftp://42.pl/"
          },
          {
            "http-field": "http://42.0.pl/",
            "ftp-field": "ftp://42.0.pl/"
          }
        ]
      },
      {
        "sample-selectors": [
          {
            "type": "columnList",
            "values": ["column1", "column2", "...", "column42"]
          },
          {
            "type": "indexList",
            "values": [0, 42]
          },
          {
            "type": "typeList",
            "values": ["numeric", "time interval", "categorical"]
          }
        ],
        "sample-single-column-selector": {
          "type": "column",
          "value": "kozik"
        },
        "sample-single-index-selector": {
          "type": "index",
          "value": 42
        }
      },
      {
        "sample_snippet": "K <- 42;",
        "sample_choice": {
          "choice1": {
            "name21": "word42"
          },
          "choice2": {
            "name12": false,
            "name22": "exit 42 * 2 - 42;"
          },
          "choice3": {
            "name31": "let codilime <- 42 * 3 - 42 * 2;"
          }
        }
      }
    ];

    for (var i = 0; i < nodes.length; ++i) {
      nodes[i].parameters = DeepsenseNodeParameters.factory.createParametersList(parameterValues[i], parameterSchemas[i]);
    }

    return {
      getNodes: function () {
        return nodes;
      }
    };
  }]);
