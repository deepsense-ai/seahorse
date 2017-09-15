/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

describe('experiment', () => {
  let Experiment;
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
    angular.mock.inject((_Experiment_, _OperationsHierarchyService_) => {
      Experiment = _Experiment_;
      OperationsHierarchyService = _OperationsHierarchyService_;
    });
  });

  it('should be defined', () => {
    expect(Experiment).toBeDefined();
    expect(Experiment).toEqual(jasmine.any(Function));
  });

  it('has getId method', () => {
    let experiment = new Experiment();

    experiment.setData({
      'id': initId,
      'name': initName,
      'description': initDescription
    });

    expect(experiment.getId()).toBe(initId);
  });

  it('can create edges', () => {
    let experiment = new Experiment();

    experiment.createNodes(initNodes, initOperations, initState);
    expect(Object.keys(experiment.getEdges()).length).toBe(0);

    experiment.createEdges(initConnections);
    expect(Object.keys(experiment.getEdges()).length).toBe(1);
  });

  it('can create nodes and edges', () => {
    let experiment = new Experiment();

    experiment.addNode(experiment.createNode({
      'id': initNodes[0].id,
      'operation': initOperations[initNodes[0].operation.id],
      'parameters': initNodes[0].parameters
    }));
    experiment.addNode(experiment.createNode({
      'id': initNodes[1].id,
      'operation': initOperations[initNodes[1].operation.id],
      'parameters': initNodes[1].parameters
    }));
    expect(Object.keys(experiment.getNodes()).length).toBe(2);

    let edge = experiment.createEdge(initConnections[0]);
    experiment.addEdge(edge);
    expect(Object.keys(experiment.getEdges()).length).toBe(1);
  });

  it('can remove nodes and edge', () => {
    let experiment = new Experiment();

    experiment.addNode(experiment.createNode({
      'id': initNodes[0].id,
      'operation': initOperations[initNodes[0].operation.id],
      'parameters': initNodes[0].parameters
    }));
    experiment.addNode(experiment.createNode({
      'id': initNodes[1].id,
      'operation': initOperations[initNodes[1].operation.id],
      'parameters': initNodes[1].parameters
    }));

    let edge = experiment.createEdge(initConnections[0]);
    experiment.addEdge(edge);
    expect(Object.keys(experiment.getEdges()).length).toEqual(1);

    experiment.removeEdge(edge);
    expect(Object.keys(experiment.getEdges()).length).toEqual(0);

    experiment.removeNode(edge.startNodeId);
    experiment.removeNode(edge.endNodeId);
    expect(Object.keys(experiment.getNodes()).length).toEqual(0);
  });

  it('should have serialize method', () => {
    let experiment = new Experiment();
    experiment.setData({
      'id': initId,
      'name': initName,
      'description': initDescription
    });
    experiment.createNodes(initNodes, initOperations, initState);
    experiment.createEdges(initConnections);

    expect(experiment.serialize).toEqual(jasmine.any(Function));
    expect(experiment.serialize()).toEqual(serializedData);
  });

  it('handle status changes', () => {
    let experiment = new Experiment();
    expect(experiment.getStatus()).toBe(experiment.STATUS_DEFAULT);

    experiment.setStatus({
      'status': 'RUNNING'
    });
    expect(experiment.getStatus()).toBe(experiment.STATUS.RUNNING);

    experiment.setStatus();
    expect(experiment.getStatus()).toBe(experiment.STATUS.RUNNING);
    experiment.setStatus(false);
    expect(experiment.getStatus()).toBe(experiment.STATUS.RUNNING);
    experiment.setStatus({});
    expect(experiment.getStatus()).toBe(experiment.STATUS.RUNNING);
    experiment.setStatus({
      'status': 'x'
    });
    expect(experiment.getStatus()).toBe(experiment.STATUS.RUNNING);
  });

  it('return run state', () => {
    let experiment = new Experiment();
    expect(experiment.isRunning()).toBe(false);

    experiment.setStatus({
      'status': 'RUNNING'
    });
    expect(experiment.isRunning()).toBe(true);

    experiment.setStatus();
    expect(experiment.isRunning()).toBe(true);

    experiment.setStatus({
      'status': 'COMPLETED'
    });
    expect(experiment.isRunning()).toBe(false);
  });

  it('update nodes state', () => {
    function checkNodeStatuses(experiment, statuses) {
      let nodes = experiment.getNodes();
      for (let id in nodes) {
        expect(nodes[id].status).toBe(statuses[id]);
      }
    }

    let experiment = new Experiment();
    experiment.createNodes(initNodes, initOperations, initState);
    let STATUS = experiment.getNodes()[initNodes[0].id].STATUS;

    expect(experiment.isRunning()).toBe(false);
    checkNodeStatuses(experiment, {
      '101': STATUS.DRAFT,
      '102': STATUS.DRAFT
    });

    experiment.updateState({
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
    expect(experiment.isRunning()).toBe(true);
    checkNodeStatuses(experiment, {
      '101': STATUS.RUNNING,
      '102': STATUS.QUEUED
    });

    experiment.updateState({});
    expect(experiment.isRunning()).toBe(true);
    checkNodeStatuses(experiment, {
      '101': STATUS.RUNNING,
      '102': STATUS.QUEUED
    });

    experiment.updateState({
      'status': 'FAILED',
      'nodes': {
        '101': {
          'status': 'FAILED'
        }
      }
    });
    expect(experiment.isRunning()).toBe(false);
    checkNodeStatuses(experiment, {
      '101': STATUS.FAILED,
      '102': STATUS.QUEUED
    });
  });

  it('updates type knowledge regarding output ports', () => {
    let experiment = new Experiment();
    experiment.createNodes(initNodes, initOperations, initState);

    let nodes = experiment.getNodes();
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

    experiment.updateTypeKnowledge(knowledge);

    expect(node0.input[0].typeQualifier).toEqual(initOperations.o1.ports.input[0].typeQualifier);
    expect(node0.output[0].typeQualifier).toEqual(['T02']);

    expect(node1.input[0].typeQualifier).toEqual(initOperations.o2.ports.input[0].typeQualifier);
    expect(node1.output[0].typeQualifier).toEqual(['T04']);
  });

  describe('updates edges\' states.', () => {
    let initExperiment = (experiment) => {
      experiment.createNodes(initNodes, initOperations, initState);
      experiment.createEdges(initConnections);
    };

    it('The edge\'s state equals ALWAYS', angular.mock.inject((OperationsHierarchyService) => {
      let experiment = new Experiment();
      initExperiment(experiment);

      // T21,T22 -> T31,T32
      let edge = _.values(experiment.getEdges())[0];
      experiment.updateEdgesStates(OperationsHierarchyService);
      expect(edge.state).toBe(edge.STATE_TYPE.ALWAYS);
    }));

    it('The edge\'s state equals MAYBE', angular.mock.inject((OperationsHierarchyService) => {
      let experiment = new Experiment();
      initExperiment(experiment);

      let knowledge = {
        [initNodes[0].id]: buildNodeKnowledge([['T22', 'T23']])
      };
      experiment.updateTypeKnowledge(knowledge);

      // T22,T23 -> T31,T32
      let edge = _.values(experiment.getEdges())[0];
      experiment.updateEdgesStates(OperationsHierarchyService);
      expect(edge.state).toBe(edge.STATE_TYPE.MAYBE);
    }));

    it('The edge\'s state equals MAYBE', angular.mock.inject((OperationsHierarchyService) => {
      let experiment = new Experiment();
      initExperiment(experiment);

      let knowledge = {
        [initNodes[0].id]: buildNodeKnowledge([['T23', 'T24']])
      };
      experiment.updateTypeKnowledge(knowledge);

      // T23,T24 -> T31,T32
      let edge = _.values(experiment.getEdges())[0];
      experiment.updateEdgesStates(OperationsHierarchyService);
      expect(edge.state).toBe(edge.STATE_TYPE.NEVER);
    }));
  });
});
