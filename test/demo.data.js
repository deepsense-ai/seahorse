'use strict';

angular.module('test').
  factory('Model', ['DeepsenseNodeParameters', function (DeepsenseNodeParameters) {
    var nodes = [
      {
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
            typeQualifier: ['io.deepsense.type1', 'io.seahorse.type2', 'io.deepsense.type3']
          },
          {
            portIndex: 1,
            typeQualifier: ['io.deepsense.type4', 'io.seahorse.type5']
          }
        ]
      },
      {
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
      {
        description: 'Neural networks inspired by biological STARTS test of description length message, hey hey hey this is a test so please welcome',
        id: 'edc02822-b8ee-4225-b070-f892a87dbf11',
        name: 'Neural Network',
        color: '#00B1EB',
        operationId: '11111-111111-44444',
        parameters: null,
        state: null
      },
      {
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
      {
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
      {
        description: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Aspernatur et harum neque nostrum qui similique soluta veritatis. Doloribus eligendi explicabo illo iure nostrum quas ratione soluta veritatis vero voluptatem? Rem.',
        id: 'a61a311f-ffc0-71b8-736c-3fb1e3cf8e77',
        name: 'Test draft',
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
      }
    ];

    var parameterSchemas = [
      [
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
          "name": "sample-selectors-empty",
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
      [],
      [
        {
          "name": "selectors-new",
          "type": "selector",
          "description": "Selector for new design",
          "port": 1,
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
      [
        {
          "name": "selectors-single-test",
          "type": "selector",
          "description": "sample desc",
          "port": 1,
          "default": null,
          "required": true,
          "isSingle": true
        }
      ]
    ];

    var parameterValues = [
      {
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
      },
      {
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
        "sample-single-column-selector": {
          "type": "column", // name
          "value": "kozik"
        },
        "sample-single-index-selector": {
          "type": "index",
          "value": 42
        }
      },
      {
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
      },
      {},
      {
        "selectors-new": {
          "excluding": false,
          "selections": []
        },
        "single-choice": {
          "option2": {}
        }
      },
      {
        "selectors-single-test": {
          "type": "index",
          "value": 1
        }
      }
    ];

    for (var i = 0; i < nodes.length; ++i) {
      nodes[i].parameters = DeepsenseNodeParameters.factory.createParametersList(parameterValues[i], parameterSchemas[i]);
    }

    let workflow_id = "mock_workflow_id";

    return {
      getNodes: () => nodes,
      getWorkflowId: () => workflow_id
    };
  }]);
