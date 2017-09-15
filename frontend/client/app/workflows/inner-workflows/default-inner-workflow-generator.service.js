/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
