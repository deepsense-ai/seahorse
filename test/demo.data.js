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

    var parameters = [
      {
        "parameters": {
          "connection_string": {
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
          "boolean_attr_test": {
            "type": "boolean",
            "description": "boolean attr test",
            "default": true,
            "required": true
          },
          "sample_multiple_choice": {
            "type": "multipleChoice",
            "description": "sample multiple choice description",
            "default": ["choice2-some-example-text"],
            "required": true,
            "values": {
              "choice1-long-text": {
                "name_001": {
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
                      "endIncluded": false,
                      "step": 1
                    }
                  }
                }
              },
              "choice2-some-example-text": {
                "name_002": {
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
              }
            }
          },
          "sample-multiplier": {
            "type": "multiplier",
            "description": "This is a multiplier parameter.",
            "required": true,
            "values": {
              "http-field": {
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
              "ftp-field": {
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
            }
          }
        }
      },
      {
        "parameters": {
          "sample-selectors": {
            "type": "selector",
            "description": "sample desc",
            "port": 1,
            "default": null,
            "required": true,
            "isSingle": false
          },
          "sample-single-index-selector": {
            "type": "selector",
            "description": "sample desc",
            "port": 1,
            "default": null,
            "required": true,
            "isSingle": true
          },
          "sample-single-column-selector": {
            "type": "selector",
            "description": "sample desc",
            "port": 1,
            "default": null,
            "required": true,
            "isSingle": true
          },
          "condition_threshold": {
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
        }
      },
      {
        "parameters": {
          "sample_snippet": {
            "type": "snippet",
            "description": "This is a snippet parameter.",
            "language": "R",
            "default": "m <- 1000",
            "required": true
          },
          "sample_choice": {
            "type": "choice",
            "description": "sample choice description",
            "default": "choice1",
            "required": true,
            "values": {
              "choice1": {
                "name11": {
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
                "name21": {
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
              },
              "choice2": {
                "name12": {
                  "type": "boolean",
                  "description": "12 desc",
                  "default": true,
                  "required": false
                },
                "name22": {
                  "type": "snippet",
                  "description": "22 desc",
                  "language": "R",
                  "default": "exit 42;",
                  "required": true
                }
              },
              "choice3": {
                "name31": {
                  "type": "snippet",
                  "description": "31 desc",
                  "language": "R",
                  "default": "let codilime <- 42;",
                  "required": true
                }
              }
            }
          }
        }
      }
    ];

    for (var i = 0; i < nodes.length; ++i) {
      nodes[i].parameters = DeepsenseNodeParameters.factory.createParametersList({}, parameters[i].parameters);
    }

    return {
      getNodes: function () {
        return nodes;
      }
    };
  }]);
