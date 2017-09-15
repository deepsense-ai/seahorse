'use strict';

/* @ngInject */
function DefaultInnerWorkflowGenerator(UUIDGenerator) {

  return {
    create: () => {
      return {
        'id': UUIDGenerator.generateUUID(),
        'workflow': {
          'nodes': [{
            'id': '2603a7b5-aaa9-40ad-9598-23f234ec5c32',
            'operation': {
              'id': 'f94b04d7-ec34-42f7-8100-93fe235c89f8',
              'name': 'Source'
            },
            'parameters': {}
          }, {
            'id': 'd7798d5e-b1c6-4027-873e-a6d653957418',
            'operation': {
              'id': 'e652238f-7415-4da6-95c6-ee33808561b2',
              'name': 'Sink'
            },
            'parameters': {}
          }],
          'connections': [{
            'from': {
              'nodeId': '2603a7b5-aaa9-40ad-9598-23f234ec5c32',
              'portIndex': 0
            },
            'to': {
              'nodeId': 'd7798d5e-b1c6-4027-873e-a6d653957418',
              'portIndex': 0
            }
          }]
        },
        'thirdPartyData': {
          'gui': {
            'name': 'Inner workflow of custom transformer',
            'predefColors': ['#00B1EB', '#1ab394', '#2f4050', '#f8ac59', '#ed5565', '#DD6D3F'],
            'nodes': {
              '2603a7b5-aaa9-40ad-9598-23f234ec5c32': {
                'uiName': '',
                'color': '#2F4050',
                'coordinates': {
                  'x': 5233,
                  'y': 4951
                }
              },
              'd7798d5e-b1c6-4027-873e-a6d653957418': {
                'uiName': '',
                'color': '#2F4050',
                'coordinates': {
                  'x': 5236,
                  'y': 5247
                }
              }
            }
          }
        },
        'variables': {}
      };
    }

  };

}

exports.function = DefaultInnerWorkflowGenerator;

exports.inject = function(module) {
  module.factory('DefaultInnerWorkflowGenerator', DefaultInnerWorkflowGenerator);
};
