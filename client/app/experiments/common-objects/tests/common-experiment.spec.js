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

  it('can create nodes and edges', () => {
    let experiment = new Experiment();
    experiment.createNodes(initNodes, initOperations);
    expect(Object.keys(experiment.getEdges()).length).toBe(0);
    experiment.createEdges(initConnections);
    expect(Object.keys(experiment.getEdges()).length).toBe(1);
  });

  it('can create nodes and edge', () => {
    let experiment = new Experiment();
    experiment.addNode(experiment.createNode(initNodes[0].id, initOperations[initNodes[0].operation.id], initNodes[0].parameters));
    experiment.addNode(experiment.createNode(initNodes[1].id, initOperations[initNodes[1].operation.id], initNodes[1].parameters));
    expect(Object.keys(experiment.getNodes()).length).toBe(2);
    var edge = experiment.createEdge(initConnections[0]);
    experiment.addEdge(edge);
    expect(Object.keys(experiment.getEdges()).length).toBe(1);
  });

  it('can remove nodes and edge', () => {
    let experiment = new Experiment();
    experiment.addNode(experiment.createNode(initNodes[0].id, initOperations[initNodes[0].operation.id], initNodes[0].parameters));
    experiment.addNode(experiment.createNode(initNodes[1].id, initOperations[initNodes[1].operation.id], initNodes[1].parameters));
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
    experiment.createNodes(initNodes, initOperations);
    experiment.createEdges(initConnections);

    expect(experiment.serialize).toEqual(jasmine.any(Function));
    expect(experiment.serialize()).toEqual(serializedData);
  });

});
