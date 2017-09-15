'use strict';

/* @ngInject */
function DefaultInnerWorkflowGenerator(UUIDGenerator) {

  return {
    create: () => {
      const sourceId = UUIDGenerator.generateUUID();
      const sinkId = UUIDGenerator.generateUUID();
      return {
        'id': UUIDGenerator.generateUUID(),
        'workflow': {
          'nodes': [{
            'id': sourceId,
            'operation': {
              'id': 'f94b04d7-ec34-42f7-8100-93fe235c89f8',
              'name': 'Source'
            },
            'parameters': {}
          }, {
            'id': sinkId,
            'operation': {
              'id': 'e652238f-7415-4da6-95c6-ee33808561b2',
              'name': 'Sink'
            },
            'parameters': {}
          }],
          'connections': [{
            'from': {
              'nodeId': sourceId,
              'portIndex': 0
            },
            'to': {
              'nodeId': sinkId,
              'portIndex': 0
            }
          }]
        },
        publicParams: [],
        'thirdPartyData': {
          'gui': {
            'name': 'Inner workflow of custom transformer',
            'nodes': {
              [sourceId]: {
                'uiName': '',
                'color': '#2F4050',
                'coordinates': {
                  'x': 5233,
                  'y': 4951
                }
              },
              [sinkId]: {
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
