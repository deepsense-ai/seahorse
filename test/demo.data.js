'use strict';

angular.module('test').
  factory('Model', ['DeepsenseNodeParameters', function (DeepsenseNodeParameters) {

    let sqlReader = {
      node: {
        description: 'Simple sql db reader',
        id: '88f560a9-97e3-4fa2-93ce-edad4cab7ea9',
        name: 'Sql Reader',
        uiName: 'Sql Reader',
        color: '#00B1EB',
        operationId: 'e76ca616-0322-47a5-b390-70c9668265dd',
        parameters: null,
        state: {
          "status": "status_completed",
          "started": "2012-08-20T21:11:09Z",
          "ended": "2012-08-20T21:12:09Z"
        },
        stateDetails: {
          "progress": {
            "current": 3,
            "total": 23
          }
        },
        input: [
          {
            portIndex: 0,
            typeQualifier: ['io.deepsense.type1', 'io.seahorse.type2', 'io.deepsense.type3']
          },
          {
            portIndex: 1,
            typeQualifier: ['io.deepsense.type4', 'io.seahorse.type5']
          }
        ],
        originalOutput: [
          {
            portIndex: 0,
            typeQualifier: ['io.deepsense.type1', 'io.seahorse.type2', 'io.deepsense.type3']
          },
          {
            portIndex: 1,
            typeQualifier: ['io.deepsense.type4']
          }
        ],
        output: [
          {
            portIndex: 0,
            typeQualifier: ['io.deepsense.type1'],
            result: {
              params: {
                schema: [
                  {
                    name: "inferred_string",
                    type: "string",
                    description: "inferred string description",
                    default: "some default",
                    required: true,
                    validator: {
                      type: "regex",
                      configuration: {
                        regex: ".*"
                      }
                    }
                  },
                  {
                    name: "inferred_boolean",
                    type: "boolean",
                    description: "boolean attr test",
                    default: true,
                    required: true
                  },
                  {
                    name: "inferred_numeric",
                    type: "numeric",
                    description: "numeric attr test",
                    default: 123,
                    required: true,
                    isGriddable: true
                  },
                  {
                    "name": "inferred_choice",
                    "type": "choice",
                    "description": "sample choice description",
                    "default": "choice1",
                    "required": true,
                    "values": [
                      {
                        "name": "choice1",
                        "schema": [
                          {
                            "name": "wrapped default",
                            "type": "numeric",
                            "description": "11 desc",
                            "default": 42,
                            "required": true,
                            "isGriddable": true,
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
                            "name": "overriden default",
                            "type": "numeric",
                            "description": "11 desc",
                            "default": 42,
                            "required": true,
                            "isGriddable": true,
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
                        "name": "choice4",
                        "schema": null
                      }
                    ]
                  }
                ],
                values: {
                  "inferred_string": "inferred string value",
                  "inferred_choice": {
                    "choice1": {
                      "overriden default": {
                        "values": [{
                          "type": "seq",
                          "value": {
                            "sequence": [42, 44, 46, 48]
                          }
                        }]
                      }
                    }
                  }
                }
              }
            }
          },
          {
            portIndex: 1,
            typeQualifier: ['io.deepsense.type1', 'io.seahorse.type2', 'io.seahorse.type3']
          },
          {
            portIndex: 2,
            typeQualifier: ['io.deepsense.type3'],
            result: {
              schema: {
                fields: [
                  {
                    name: "x",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "y",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abc",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cde",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "x1",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "y2",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abc3",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cde4",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "x11",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "y22",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abc33",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cde44",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "x111",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "y222",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abc333",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cde444",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "x1111",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "y2222",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abc3333",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cde4444",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "x5",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "y6",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abc7",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cde8",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "xx",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "yy",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abcabc",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cdecde",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "xx1",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "bool:yy2",
                    dataType: "boolean",
                    deeplangType: "boolean"
                  },
                  {
                    name: "abcabc3",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "certain timestamp col",
                    dataType: "timestamp",
                    deeplangType: "timestamp"
                  },{
                    name: "xx11",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "yy22",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abcabc33",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "cdecde44",
                    dataType: "timestamp",
                    deeplangType: "timestamp"
                  },
                  {
                    name: "xx5",
                    dataType: "integer",
                    deeplangType: "numeric"
                  },
                  {
                    name: "yy6",
                    dataType: "string",
                    deeplangType: "string"
                  },
                  {
                    name: "abcabc7-bool",
                    dataType: "boolean",
                    deeplangType: "boolean"
                  },
                  {
                    name: "cdecde8",
                    dataType: "string",
                    deeplangType: "string"
                  }
                ]
              }
            }
          }
        ]
      },
      schema: [
        {
          "name": "connection_string",
          "type": "string",
          "description": "jdbc address mmmmmmmm mmmmmmmm mmmmmmmm mmmmmmmm mmmmmmmm mmmmmmmm mmmmmmmm mmmmmmmmmmmmm mmmmmmmmmmm mmmmmmmm",
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
          "name": "sample-multiplier",
          "type": "multiplier",
          "description": "This is a multiplier parameter test teats as ttatasast" +
          "asast tasat sat at .",
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
            },
            {
              "name": "some-cols",
              "type": "selector",
              "description": "sample desc",
              "default": null,
              "required": true,
              "isSingle": true,
              "portIndex": 1
            }
          ]
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
            },
            {
              "name": "choice3-no-schema",
              "schema": null
            }
          ]
        },
        {
          "name": "sample-multiplier-nonempty",
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
      values: {
        "connection_string": "jdbc:test/string/field",
        "boolean_attr_test": false,
        "sample-multiplier": [
        ],
        "sample_multiple_choice": {
          "choice1-long-text": {
            "name_001": 102
          },
          "choice2-some-example-text": {
            "name_002": "2-2=0"
          }
        },
        "sample-multiplier-nonempty": [
          {
            "http-field": "http://42.pl/",
            "ftp-field": "ftp://42.pl/"
          },
          {
            "http-field": "http://42.0.pl/",
            "ftp-field": "ftp://42.0.pl/"
          }
        ]
      }
    };

    let rowFilter = {
      node: {
        description: 'Simple filters takes dataset based on...',
        id: '210ce781-0a9f-477d-99a7-5be31f7bfc02',
        name: 'Row Filter',
        uiName: 'Row Filter',
        color: '#00B1EB',
        operationId: '342342-123123-434234-12334',
        parameters: null,
        state: {
          "status": "status_completed",
          "started": "2013-09-10T22:11:09Z",
          "ended": "2013-09-20T21:22:09Z"
        }
      },
      schema: [
        {
          "name": "sample-selectors",
          "type": "selector",
          "description": "sample desc",
          "portIndex": 1,
          "default": null,
          "required": true,
          "isSingle": false
        },
        {
          "name": "sample-selectors-empty",
          "type": "selector",
          "description": "sample desc",
          "portIndex": 1,
          "default": null,
          "required": true,
          "isSingle": false
        },
        {
          "name": "multi-selector-known-cols",
          "type": "selector",
          "description": "this tests multi-selector where columns are known",
          "portIndex": 2,
          "default": null,
          "required": true,
          "isSingle": false
        },
        {
          "name": "single-selector-known-cols",
          "type": "selector",
          "description": "this tests single-selector where columns are known",
          "portIndex": 2,
          "default": null,
          "required": true,
          "isSingle": true
        },
        {
          "name": "sample-single-index-selector",
          "type": "selector",
          "description": "sample desc",
          "portIndex": 1,
          "default": null,
          "required": true,
          "isSingle": true
        },
        {
          "name": "sample-single-column-selector",
          "type": "selector",
          "description": "sample desc",
          "portIndex": 1,
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
      values: {
        "sample-selectors": {
          "excluding": false,
          "selections": [
            {
              "type": "columnList",
              "values": ["column1", "column2", "...", "column42"]
            },
            {
              "type": "indexRange",
              "values": [0, 42]
            },
            {
              "type": "typeList",
              "values": ["numeric", "time interval", "categorical"]
            }
          ]
        },
        "sample-selectors-empty": null,
        "multi-selector-known-cols": {
          "excluding": false,
          "selections": [
            {
              "type": "columnList",
              "values": ["unknown-col", "abc"]
            }
          ]
        },
        "sample-single-column-selector": {
          "type": "column", // name
          "value": "kozik"
        },
        "sample-single-index-selector": {
          "type": "index",
          "value": 42
        }
      }
    };

    let neuralNetwork = {
      node: {
        description: 'Neural networks inspired by biological STARTS test of description length message, hey hey hey this is a test so please welcome',
        id: 'edc02822-b8ee-4225-b070-f892a87dbf11',
        name: 'Neural Network',
        color: '#00B1EB',
        operationId: '11111-111111-44444',
        parameters: null,
        state: null
      },
      schema: [
        {
          "name": "sample_codeSnippet",
          "type": "codeSnippet",
          "description": "This is a codeSnippet parameter.",
          "language": {
            "name": "Python"
          },
          "default": "def operation_main(data_frame_1):\n  return out_data_frame_1",
          "required": true
        },
        {
          "name": "sample_choice",
          "type": "multipleChoice",
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
                  "type": "codeSnippet",
                  "description": "22 desc",
                  "language": {
                    "name": "Python"
                  },
                  "default": "def operation_main(data_frame_1):\n  return out_data_frame_1",
                  "required": true
                }
              ]
            },
            {
              "name": "choice3",
              "schema": [
                {
                  "name": "name31",
                  "type": "codeSnippet",
                  "description": "31 desc",
                  "language": {
                    "name": "Python"
                  },
                  "default": "def operation_main(data_frame_1):\n  return out_data_frame_1",
                  "required": true
                }
              ]
            },
            {
              "name": "choice4",
              "schema": null
            }
          ]
        },
        {
          "name": "creator-sample-parameter",
          "type": "creator",
          "description": "This is a single column creator parameter.",
          "default": "predictions",
          "required": true
        },
        {
          "name": "prefix-based-creator-sample-parameter",
          "type": "prefixBasedCreator",
          "description": "This is a prefix-based creator parameter.",
          "default": "prefix_",
          "required": true
        }
      ],
      values: {
        "sample_codeSnippet": "K <- 42;",
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
        },
        "creator-sample-parameter": "sample text"
      }
    };

    let fileToDataframe = {
      node: {
        description: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Aspernatur et harum neque nostrum qui similique soluta veritatis. Doloribus eligendi explicabo illo iure nostrum quas ratione soluta veritatis vero voluptatem? Rem.',
        id: 'e61a300f-ffc0-71b8-736c-3fb1e3cf8e77',
        name: 'File To DataFrame',
        uiName: 'File To DataFrame',
        color: '#00B1EB',
        operationId: '11111-111111-33333',
        parameters: null,
        state: {
          "status": "status_aborted",
          "started": "2012-08-20T21:11:09Z",
          "ended": "2012-08-20T21:12:09Z",
          "error": {
            "id": "b23dd1a8-8c41-434a-a465-ebcd9d3ef114",
            "code": 42,
            "title": "Question unknown",
            "message": "Something that describes the problem more precisely",
            "details": {"stacktrace": "exception404\nexception502\nLorem ipsum dolor sit amet, consectetur adipisicing " +
            "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
            " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
            "Lorem ipsum dolor sit amet, consectetur adipisicing"
            }
          }
        }
      },
      schema: [],
      values: {}
    };

    let testDraft = {
      node: {
        description: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Aspernatur et harum neque nostrum qui similique soluta veritatis. Doloribus eligendi explicabo illo iure nostrum quas ratione soluta veritatis vero voluptatem? Rem.',
        id: 'a61a300f-ffc0-71b8-736c-3fb1e3cf8e77',
        name: 'Test draft',
        uiName: 'Test draft',
        color: '#00B1EB',
        operationId: '11111-111111-33333',
        parameters: null,
        state: {
          "status": "status_failed",
          "started": "2012-08-20T21:11:09Z",
          "ended": "2012-08-20T21:12:09Z",
          "error": {
            "id": "b23dd1a8-8c41-434a-a465-ebcd9d3ef114",
            "code": 42,
            "title": "Question unknown",
            "message": "Lorem ipsum dolor sit amet, consectetur adipisicing " +
            "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
            " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
            "aut autem consectetur deserunt, dolore, eius eum facilis fugiat" +
            " impedit magnam maxime odio omnis reiciendis suscipit tempora " +
            "temporibus tenetur totam unde, voluptatum. Architecto, dolorum?",
            "details": {
              "stacktrace": "exception404\nexception502\nLorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid "
              +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid "
              +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid "
              +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid "
              +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid " +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi\n" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid \n" +
              "Lorem ipsum dolor sit amet, consectetur adipisicing " +
              "elit. Animi aut ducimus eaque error, fuga in ipsam magnam modi" +
              " mollitia placeat provident, quasi qui reiciendis! Ad aliquid "
            }
          }
        },
        stateDetails: null
      },
      schema: [
        {
          "name": "selectors-new",
          "type": "selector",
          "description": "Selector for new design",
          "portIndex": 1,
          "default": null,
          "required": true,
          "isSingle": false
        },
        {
          "name": "single-choice",
          "type": "choice",
          "description": "that's the single choice parameter, enjoy!",
          "default": "option1",
          "required": true,
          "values": [
            {
              "name": "option1",
              "schema": null
            },
            {
              "name": "option2",
              "schema": null
            },
            {
              "name": "option3",
              "schema": null
            }
          ]
        }
      ],
      values: {
        "selectors-new": {
          "excluding": false,
          "selections": []
        },
        "single-choice": {
          "option2": {}
        }
      }
    };

    let dynamicParams = {
      node: {
        description: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Aspernatur et harum neque nostrum qui similique soluta veritatis. Doloribus eligendi explicabo illo iure nostrum quas ratione soluta veritatis vero voluptatem? Rem.',
        id: 'a61a311f-ffc0-71b8-736c-3fb1e3cf8e77',
        name: 'Test of dynamic params',
        uiName: '',
        color: '#00B1EB',
        operationId: '11111-111111-33333',
        parameters: null,
        state: {
          "status": "status_failed",
          "started": "2012-08-20T21:11:09Z",
          "ended": "2012-08-20T21:12:09Z",
          "error": {
            "id": "b23dd1a8-8c41-434a-a465-ebcd9d3ef114",
            "code": 42,
            "title": "Question unknown",
            "message": "Lorem ipsum dolor sit amet, consectetur adipisicing" +
            " elit. Alias eligendi error nam nulla optio? Aspernatur " +
            "dignissimos enim facere impedit ipsa ipsam labore, minima " +
            "nostrum officiis quidem repellat saepe, sit unde. Ad adipisci " +
            "commodi cum dolorum iusto magnam mollitia nulla vitae, " +
            "voluptatibus. Adipisci aperiam assumenda consequuntur cum dolor " +
            "eveniet ex exercitationem harum hic ipsam iusto nostrum quas " +
            "reprehenderit sapiente sed, totam velit, veritatis! Asperiores " +
            "autem consequatur, cupiditate deserunt dolorem dolorum fugit " +
            "iure magnam maxime nemo, non placeat possimus. Architecto " +
            "assumenda error fuga fugit impedit nemo nihil optio quam quo " +
            "soluta. Autem deleniti exercitationem expedita explicabo labore " +
            "laboriosam laudantium reiciendis tenetur voluptatum.",
            "details": {
            }
          }
        },
        stateDetails: null
      },
      schema: [
        {
          "name": "selectors-single-test",
          "type": "selector",
          "description": "sample desc",
          "portIndex": 1,
          "default": null,
          "required": true,
          "isSingle": true
        },
        {
          name: "dynamic-param-test",
          type: "dynamic",
          description: "this tests dynamic param when params are inferred",
          inputPort: 0,
          default: null,
          required: true
        },
        {
          name: "dynamic-param-test-1",
          type: "dynamic",
          description: "this tests dynamic param when params are NOT inferred",
          inputPort: 1,
          default: null,
          required: true
        }
      ],
      values: {
        "selectors-single-test": {
          "type": "index",
          "value": 1
        }
      }
    };

    let workflowParam = {
      node:  {
        name: 'Workflow param',
        description: 'Test of workflow param',
        id: '52a4a9fe-8d8e-4dcc-81f7-d1d34352b969',
        color: '#00B1EB',
        operationId: '11111-111111-55555',
        parameters: null,
        state: null
      },
      schema: [
        {
          "name": "Sample workflow",
          "type": "workflow",
          "description": "This is workflow parameter",
          "default": "asd",
          "required": true
        }
      ],
      values: {"Sample workflow": {}}
    };

    let gridsearchParams = {
      node: {
        description: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Aspernatur et harum neque nostrum qui similique soluta veritatis. Doloribus eligendi explicabo illo iure nostrum quas ratione soluta veritatis vero voluptatem? Rem.',
        id: 'a61a311f-ffc0-71b8-736c-3fb184df8e77',
        name: 'Test of gridsearch params',
        uiName: '',
        color: '#00B1EB',
        operationId: '11111-111111-33333',
        parameters: null,
        state: {
          "status": "status_failed",
          "started": "2012-08-20T21:11:09Z",
          "ended": "2012-08-20T21:12:09Z",
          "error": {
            "id": "b23dd1a8-8c41-434a-a465-ebcd9d3ef114",
            "code": 42,
            "title": "Question unknown",
            "message": "Lorem ipsum dolor sit amet, consectetur adipisicing" +
            " elit. Alias eligendi error nam nulla optio? Aspernatur " +
            "dignissimos enim facere impedit ipsa ipsam labore, minima " +
            "nostrum officiis quidem repellat saepe, sit unde. Ad adipisci " +
            "commodi cum dolorum iusto magnam mollitia nulla vitae, " +
            "voluptatibus. Adipisci aperiam assumenda consequuntur cum dolor " +
            "eveniet ex exercitationem harum hic ipsam iusto nostrum quas " +
            "reprehenderit sapiente sed, totam velit, veritatis! Asperiores " +
            "autem consequatur, cupiditate deserunt dolorem dolorum fugit " +
            "iure magnam maxime nemo, non placeat possimus. Architecto " +
            "assumenda error fuga fugit impedit nemo nihil optio quam quo " +
            "soluta. Autem deleniti exercitationem expedita explicabo labore " +
            "laboriosam laudantium reiciendis tenetur voluptatum.",
            "details": {
            }
          }
        },
        stateDetails: null
      },
      schema: [
        {
          "name": "selectors-single-test",
          "type": "selector",
          "description": "sample desc",
          "portIndex": 1,
          "default": null,
          "required": true,
          "isSingle": true
        },
        {
          name: "multiple-numeric-test",
          type: "multipleNumeric",
          description: "This tests standalone multiple numeric param",
          default: null,
          required: true
        },
        {
          name: "gridsearch-param-test",
          type: "gridSearch",
          description: "this tests gridSearch param when params are inferred",
          inputPort: 0,
          default: null,
          required: true
        },
        {
          name: "gridsearch-param-test-1",
          type: "gridSearch",
          description: "this tests gridSearch param when params are NOT inferred",
          inputPort: 1,
          default: null,
          required: true
        }
      ],
      values: {
        "selectors-single-test": {
          "type": "index",
          "value": 1
        },
        "multiple-numeric-test": {
          values: [{
            type: "seq",
            value: {
              sequence: [ -0.1, 0.0, 0.1 ]
            }
          }]
        }
      }
    };

    // Mocked node. It can return its id and incomingKnowledge.
    // In this mocked implementation, incomingKnowledge returns outputKnowledge
    // from sqlReader.node, from portIndex that is specified.
    let mockNode = {
      getIncomingKnowledge: portIndex => sqlReader.node.output[portIndex]
    };

    let testCases = [sqlReader, rowFilter, neuralNetwork, fileToDataframe, testDraft, dynamicParams, workflowParam,
      gridsearchParams];

    _.forEach(testCases, tc => {
      tc.node.parameters = DeepsenseNodeParameters.factory.createParametersList(tc.values, tc.schema, mockNode);
    });

    let nodes = _.map(testCases, tc => tc.node);

    let workflow_id = "mock_workflow_id";

    return {
      getNodes: () => nodes,
      getWorkflowId: () => workflow_id
    };
  }]);
