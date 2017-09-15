/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';


describe('experiment', () => {
  var Experiment = require('../common-experiment.js');

  var initId = '111-111-111',
      initName = 'Sample name',
      initDescription = 'Sample description...',
      initNodes = [
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
      ],
      initOperations = {
        'o1': {
          'id': 'o1',
          'name': 'Operation1',
          'version': '1',
          'ports': {
            'input': [
              {
                'portIndex': 0
              }
            ],
            'output': [
              {
                'portIndex': 0
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
                'portIndex': 0
              }
            ],
            'output': [
              {
                'portIndex': 0
              }
            ]
          },
          'parameters': {}
        }
      },
      initConnections = [
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
      ],
      initState = {
        'status': 'INDRAFT',
        'nodes': {
          '101': {
            'status': 'INDRAFT'
          },
          '102': {
            'status': 'INDRAFT'
          }
        }
      },
      serializedData = {
        'id': initId,
        'name': initName,
        'description': initDescription,
        'graph': {
          'nodes': initNodes,
          'edges': initConnections
        }
      };

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
    var edge = experiment.createEdge(initConnections[0]);
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
    var edge = experiment.createEdge(initConnections[0]);
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
      '101': STATUS.INDRAFT,
      '102': STATUS.INDRAFT,
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
      '102': STATUS.QUEUED,
    });

    experiment.updateState({});
    expect(experiment.isRunning()).toBe(true);
    checkNodeStatuses(experiment, {
      '101': STATUS.RUNNING,
      '102': STATUS.QUEUED,
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
      '102': STATUS.QUEUED,
    });
  });

});
