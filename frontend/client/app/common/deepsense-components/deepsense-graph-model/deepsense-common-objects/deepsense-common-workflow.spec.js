/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

require('./deepsense-common-workflow.js');

describe('workflow', () => {
  let Workflow;
  let OperationsHierarchyService;
  let initId = '111-111-111';
  let initName = 'Sample name';
  let initDescription = 'Sample description...';
  let initNodes = [
    {
      'id': '101',
      'operation': {
        'id': 'o1',
        'name': 'Operation1',
        'version': '1'
      },
      'parameters': {},
      'y': 100,
      'x': 200
    },
    {
      'id': '102',
      'operation': {
        'id': 'o2',
        'name': 'Operation2',
        'version': '2'
      },
      'parameters': {},
      'y': 100,
      'x': 200
    }
  ];
  let initOperations = {
    'o1': {
      'id': 'o1',
      'name': 'Operation1',
      'version': '1',
      'ports': {
        'input': [
          {
            'portIndex': 0,
            'typeQualifier': ['T1']
          }
        ],
        'output': [
          {
            'portIndex': 0,
            'typeQualifier': ['T21', 'T22']
          }
        ]
      },
      'parameters': {}
    },
    'o2': {
      'id': 'o2',
      'name': 'Operation2',
      'version': '2',
      'ports': {
        'input': [
          {
            'portIndex': 0,
            'typeQualifier': ['T31', 'T32']
          }
        ],
        'output': [
          {
            'portIndex': 0,
            'typeQualifier': ['T4']
          }
        ]
      },
      'parameters': {}
    }
  };
  let initConnections = [
    {
      'from': {
        'nodeId': '101',
        'portIndex': 0
      },
      'to': {
        'nodeId': '102',
        'portIndex': 0
      }
    }
  ];
  let initState = {
    'gui': {
      'nodes': {
        '101': {
          'status': 'DRAFT',
          'coordinates': {
            'y': 100,
            'x': 200
          }
        },
        '102': {
          'status': 'DRAFT',
          'coordinates': {
            'y': 100,
            'x': 200
          }
        }
      }
    }
  };
  let buildNodeKnowledge = (typeKnowledge) => {
    return {
      'metadata': [],
      'errors': [],
      'warnings': [],
      'ports': typeKnowledge
    };
  };

  beforeEach(() => {
    angular.mock.module('deepsense.graph-model');
    angular.mock.module(($provide) => {
      $provide.factory('OperationsHierarchyService', () => {
        return {
          IsDescendantOf: (node, ancestors) => {
            let matrix = {
              'T21': {
                'T31': true,
                'T32': true
              },
              'T22': {
                'T31': true,
                'T32': true
              },
              'T23': {
                'T31': true
              }
            };

            let result = true;
            _.each(ancestors, (ancestor) => {
              if (!(matrix[node] && matrix[node][ancestor])) {
                result = false;
              }
            });
            return result;
          }
        };
      });
    });
    angular.mock.inject((_Workflow_, _OperationsHierarchyService_) => {
      Workflow = _Workflow_;
      OperationsHierarchyService = _OperationsHierarchyService_;
    });
  });

  it('should be defined', () => {
    expect(Workflow).toBeDefined();
    expect(Workflow).toEqual(jasmine.any(Function));
  });

  it('can create edges', () => {
    let workflow = new Workflow();

    workflow.createNodes(initNodes, initOperations, initState);
    expect(Object.keys(workflow.getEdges()).length).toBe(0);

    workflow.createEdges(initConnections);
    expect(Object.keys(workflow.getEdges()).length).toBe(1);
  });

  it('can create nodes and edges', () => {
    let workflow = new Workflow();

    workflow.addNode(workflow.createNode({
      'id': initNodes[0].id,
      'operation': initOperations[initNodes[0].operation.id],
      'parameters': initNodes[0].parameters
    }));
    workflow.addNode(workflow.createNode({
      'id': initNodes[1].id,
      'operation': initOperations[initNodes[1].operation.id],
      'parameters': initNodes[1].parameters
    }));
    expect(Object.keys(workflow.getNodes()).length).toBe(2);

    let edge = workflow.createEdge(initConnections[0]);
    workflow.addEdge(edge);
    expect(Object.keys(workflow.getEdges()).length).toBe(1);
  });

  it('can remove nodes and edge', () => {
    let workflow = new Workflow();

    workflow.addNode(workflow.createNode({
      'id': initNodes[0].id,
      'operation': initOperations[initNodes[0].operation.id],
      'parameters': initNodes[0].parameters
    }));
    workflow.addNode(workflow.createNode({
      'id': initNodes[1].id,
      'operation': initOperations[initNodes[1].operation.id],
      'parameters': initNodes[1].parameters
    }));

    let edge = workflow.createEdge(initConnections[0]);
    workflow.addEdge(edge);
    expect(Object.keys(workflow.getEdges()).length).toEqual(1);

    workflow.removeEdge(edge);
    expect(Object.keys(workflow.getEdges()).length).toEqual(0);

    workflow.removeNode(edge.startNodeId);
    workflow.removeNode(edge.endNodeId);
    expect(Object.keys(workflow.getNodes()).length).toEqual(0);
  });

  it('should have serialize method', () => {
    let workflow = new Workflow();
    workflow.id = initId;
    workflow.name = initName;
    workflow.description = initDescription;

    workflow.createNodes(initNodes, initOperations, initState);
    workflow.createEdges(initConnections);

    expect(workflow.serialize).toEqual(jasmine.any(Function));
    // expect(workflow.serialize()).toEqual(serializedData);
  });

  it('update nodes state', () => {
    function checkNodeStatuses(workflow, statuses) {
      let nodes = workflow.getNodes();
      for (let id in nodes) {
        expect(nodes[id].state && nodes[id].state.status).toBe(statuses[id]);
      }
    }

    let workflow = new Workflow();
    workflow.createNodes(initNodes, initOperations, initState);
    let STATUS = workflow.getNodes()[initNodes[0].id].STATUS;

    checkNodeStatuses(workflow, {
      '101': null,
      '102': null
    });

    workflow.updateState({
      'status': 'RUNNING',
      'nodes': {
        '101': {
          'status': 'RUNNING'
        },
        '102': {
          'status': 'QUEUED'
        }
      }
    });
    checkNodeStatuses(workflow, {
      '101': STATUS.RUNNING,
      '102': STATUS.QUEUED
    });

    workflow.updateState({});
    checkNodeStatuses(workflow, {
      '101': STATUS.RUNNING,
      '102': STATUS.QUEUED
    });

    workflow.updateState({
      'status': 'FAILED',
      'nodes': {
        '101': {
          'status': 'FAILED'
        }
      }
    });
    checkNodeStatuses(workflow, {
      '101': STATUS.FAILED,
      '102': STATUS.QUEUED
    });
  });

  it('updates type knowledge regarding output ports', () => {
    let workflow = new Workflow();
    workflow.createNodes(initNodes, initOperations, initState);

    let nodes = workflow.getNodes();
    let node0 = nodes[initNodes[0].id];
    let node1 = nodes[initNodes[1].id];

    expect(node0.input[0].typeQualifier).toEqual(initOperations.o1.ports.input[0].typeQualifier);
    expect(node0.output[0].typeQualifier).toEqual(initOperations.o1.ports.output[0].typeQualifier);

    expect(node1.input[0].typeQualifier).toEqual(initOperations.o2.ports.input[0].typeQualifier);
    expect(node1.output[0].typeQualifier).toEqual(initOperations.o2.ports.output[0].typeQualifier);

    let knowledge = {
      [node0.id]: buildNodeKnowledge([{types: ['T02', 'T03']}]),
      [node1.id]: buildNodeKnowledge([{types: ['T04'], result: 'xyz'}])
    };

    workflow.updateTypeKnowledge(knowledge);

    expect(node0.input[0].typeQualifier).toEqual(initOperations.o1.ports.input[0].typeQualifier);
    expect(node0.output[0].typeQualifier).toEqual(['T02', 'T03']);

    expect(node1.input[0].typeQualifier).toEqual(initOperations.o2.ports.input[0].typeQualifier);
    expect(node1.output[0].typeQualifier).toEqual(['T04']);
    expect(node1.output[0].result).toEqual('xyz');
  });

});
