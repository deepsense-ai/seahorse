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
          'parameters': {
            'param': 1
          }
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
          'parameters': {
            'column_name': 'other'
          }
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
          }
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
          }
        }
      },
      initConnections = [
        {
          'from': {
            'node': '101',
            'portIndex': 0
          },
          'to': {
            'node': '102',
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


  it('should have serialaze method', () => {
   let experiment = new Experiment();
    experiment.setData({
      'id': initId,
      'name': initName,
      'description': initDescription
    });
    experiment.createNodes(initNodes, initOperations);
    experiment.createConnections(initConnections);

    expect(experiment.serialize).toEqual(jasmine.any(Function));
    expect(experiment.serialize()).toEqual(serializedData);
  });

});
