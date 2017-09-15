/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

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
      'ui': {
        'x': 100,
        'y': 200
      },
      'parameters': {}
    },
    {
      'id': '102',
      'operation': {
        'id': 'o2',
        'name': 'Operation2',
        'version': '2'
      },
      'ui': {
        'x': 200,
        'y': 300
      },
      'parameters': {}
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
    'status': 'DRAFT',
    'nodes': {
      '101': {
        'status': 'DRAFT'
      },
      '102': {
        'status': 'DRAFT'
      }
    }
  };
  let serializedData = {
    'id': initId,
    'name': initName,
    'description': initDescription,
    'graph': {
      'nodes': initNodes,
      'edges': initConnections
    }
  };
  let buildNodeKnowledge = (typeKnowledge) => {
    return {
      'typeKnowledge': typeKnowledge,
      'metadata': [],
      'errors': [],
      'warnings': []
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

  it('has getId method', () => {
    let workflow = new Workflow();

    workflow.setData({
      'id': initId,
      'name': initName,
      'description': initDescription
    });

    expect(workflow.getId()).toBe(initId);
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
    workflow.setData({
      'id': initId,
      'name': initName,
      'description': initDescription
    });
    workflow.createNodes(initNodes, initOperations, initState);
    workflow.createEdges(initConnections);

    expect(workflow.serialize).toEqual(jasmine.any(Function));
    expect(workflow.serialize()).toEqual(serializedData);
  });

  it('handle status changes', () => {
    let workflow = new Workflow();
    expect(workflow.getStatus()).toBe(workflow.STATUS_DEFAULT);

    workflow.setStatus({
      'status': 'RUNNING'
    });
    expect(workflow.getStatus()).toBe(workflow.STATUS.RUNNING);

    workflow.setStatus();
    expect(workflow.getStatus()).toBe(workflow.STATUS.RUNNING);
    workflow.setStatus(false);
    expect(workflow.getStatus()).toBe(workflow.STATUS.RUNNING);
    workflow.setStatus({});
    expect(workflow.getStatus()).toBe(workflow.STATUS.RUNNING);
    workflow.setStatus({
      'status': 'x'
    });
    expect(workflow.getStatus()).toBe(workflow.STATUS.RUNNING);
  });

  it('return run state', () => {
    let workflow = new Workflow();
    expect(workflow.isRunning()).toBe(false);

    workflow.setStatus({
      'status': 'RUNNING'
    });
    expect(workflow.isRunning()).toBe(true);

    workflow.setStatus();
    expect(workflow.isRunning()).toBe(true);

    workflow.setStatus({
      'status': 'COMPLETED'
    });
    expect(workflow.isRunning()).toBe(false);
  });

  it('update nodes state', () => {
    function checkNodeStatuses(workflow, statuses) {
      let nodes = workflow.getNodes();
      for (let id in nodes) {
        expect(nodes[id].status).toBe(statuses[id]);
      }
    }

    let workflow = new Workflow();
    workflow.createNodes(initNodes, initOperations, initState);
    let STATUS = workflow.getNodes()[initNodes[0].id].STATUS;

    expect(workflow.isRunning()).toBe(false);
    checkNodeStatuses(workflow, {
      '101': STATUS.DRAFT,
      '102': STATUS.DRAFT
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
    expect(workflow.isRunning()).toBe(true);
    checkNodeStatuses(workflow, {
      '101': STATUS.RUNNING,
      '102': STATUS.QUEUED
    });

    workflow.updateState({});
    expect(workflow.isRunning()).toBe(true);
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
    expect(workflow.isRunning()).toBe(false);
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
      [node0.id]: buildNodeKnowledge([['T02']]),
      [node1.id]: buildNodeKnowledge([['T04']])
    };

    workflow.updateTypeKnowledge(knowledge);

    expect(node0.input[0].typeQualifier).toEqual(initOperations.o1.ports.input[0].typeQualifier);
    expect(node0.output[0].typeQualifier).toEqual(['T02']);

    expect(node1.input[0].typeQualifier).toEqual(initOperations.o2.ports.input[0].typeQualifier);
    expect(node1.output[0].typeQualifier).toEqual(['T04']);
  });

  describe('updates edges\' states.', () => {
    let initWorkflow = (workflow) => {
      workflow.createNodes(initNodes, initOperations, initState);
      workflow.createEdges(initConnections);
    };

    it('The edge\'s state equals ALWAYS', angular.mock.inject((OperationsHierarchyService) => {
      let workflow = new Workflow();
      initWorkflow(workflow);

      // T21,T22 -> T31,T32
      let edge = _.values(workflow.getEdges())[0];
      workflow.updateEdgesStates(OperationsHierarchyService);
      expect(edge.state).toBe(edge.STATE_TYPE.ALWAYS);
    }));

    it('The edge\'s state equals MAYBE', angular.mock.inject((OperationsHierarchyService) => {
      let workflow = new Workflow();
      initWorkflow(workflow);

      let knowledge = {
        [initNodes[0].id]: buildNodeKnowledge([['T22', 'T23']])
      };
      workflow.updateTypeKnowledge(knowledge);

      // T22,T23 -> T31,T32
      let edge = _.values(workflow.getEdges())[0];
      workflow.updateEdgesStates(OperationsHierarchyService);
      expect(edge.state).toBe(edge.STATE_TYPE.MAYBE);
    }));

    it('The edge\'s state equals MAYBE', angular.mock.inject((OperationsHierarchyService) => {
      let workflow = new Workflow();
      initWorkflow(workflow);

      let knowledge = {
        [initNodes[0].id]: buildNodeKnowledge([['T23', 'T24']])
      };
      workflow.updateTypeKnowledge(knowledge);

      // T23,T24 -> T31,T32
      let edge = _.values(workflow.getEdges())[0];
      workflow.updateEdgesStates(OperationsHierarchyService);
      expect(edge.state).toBe(edge.STATE_TYPE.NEVER);
    }));
  });
});
