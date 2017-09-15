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

require('./deepsense-cycle-analyser.js');


describe('cycleAnalyser', () => {
  let DeepsenseCycleAnalyser;
  let Experiment;
  let experiment;
  let createNode = (experiment, nodeID, inputPorts, outputPorts) => {
    const TYPE_QUALIFIER = 'T';
    let node = experiment.createNode({
      id: nodeID,
      'operation': {
        'ports': {
          'input': _.map(inputPorts, (inputPort) => {
            return {
              'portIndex': inputPort,
              'typeQualifier': [TYPE_QUALIFIER]
            };
          }),
          'output': _.map(outputPorts, (outputPort) => {
            return {
              'portIndex': outputPort,
              'typeQualifier': [TYPE_QUALIFIER]
            };
          })
        }
      }
    });

    experiment.addNode(node);
  };
  let createEdge = (experiment, fromNodeID, fromOutputPortID, toNodeID, toInputPortID) => {
    let edge = experiment.createEdge({
      'from': {
        'nodeId': fromNodeID,
        'portIndex': fromOutputPortID
      },
      'to': {
        'nodeId': toNodeID,
        'portIndex': toInputPortID
      }
    });

    experiment.addEdge(edge);
  };

  beforeEach(() => {
    angular.mock.module('deepsense.cycle-analyser');
    angular.mock.inject((_DeepsenseCycleAnalyser_, _Workflow_) => {
      DeepsenseCycleAnalyser = _DeepsenseCycleAnalyser_;
      Experiment = _Workflow_;
    });

    experiment = new Experiment();
  });

  it('should be defined', () => {
    expect(DeepsenseCycleAnalyser).toBeDefined();
    expect(DeepsenseCycleAnalyser).toEqual(jasmine.any(Object));
  });

  describe('should have cycleExists method', () => {
    it('', () => {
      expect(DeepsenseCycleAnalyser.cycleExists).toEqual(jasmine.any(Function));
    });

    it('which detects a simple cycle', () => {
      // A -> B -> C -> A
      createNode(experiment, 'A', [0], [0]);
      createNode(experiment, 'B', [0], [0]);
      createNode(experiment, 'C', [0], [0]);

      createEdge(experiment, 'A', 0, 'B', 0);
      createEdge(experiment, 'B', 0, 'C', 0);
      createEdge(experiment, 'C', 0, 'A', 0);

      expect(DeepsenseCycleAnalyser.cycleExists(experiment)).toBe(true);
    });

    it('which does not detect any cycle', () => {
      createNode(experiment, 'A', [0], [0, 1]);
      createNode(experiment, 'B', [0], [0]);
      createNode(experiment, 'C', [0], [0]);
      createNode(experiment, 'D', [0, 1], [0]);

      createEdge(experiment, 'A', 0, 'B', 0);
      createEdge(experiment, 'A', 1, 'C', 0);
      createEdge(experiment, 'B', 0, 'D', 0);
      createEdge(experiment, 'C', 0, 'D', 0);

      expect(DeepsenseCycleAnalyser.cycleExists(experiment)).toBe(false);
    });

    it('which detects a cycle in more complex graph', () => {
      createNode(experiment, 'A', [0], [0, 1]);
      createNode(experiment, 'B', [0], [0, 1]);
      createNode(experiment, 'C', [0, 1], [0, 1]);
      createNode(experiment, 'D', [0, 1], []);
      createNode(experiment, 'E', [0, 1, 2], []);
      createNode(experiment, 'F', [0], [0]);
      createNode(experiment, 'G', [0], [0, 1, 2]);

      createEdge(experiment, 'A', 0, 'B', 0);
      createEdge(experiment, 'A', 0, 'C', 0);
      createEdge(experiment, 'B', 0, 'F', 0);
      createEdge(experiment, 'B', 1, 'E', 0);
      createEdge(experiment, 'C', 0, 'D', 0);
      createEdge(experiment, 'C', 1, 'E', 1);
      createEdge(experiment, 'F', 0, 'G', 0);
      createEdge(experiment, 'G', 0, 'D', 0);
      createEdge(experiment, 'G', 1, 'E', 0);
      createEdge(experiment, 'G', 2, 'A', 0);

      expect(DeepsenseCycleAnalyser.cycleExists(experiment)).toBe(true);
    });

    it('which does not detect a cycle in more complex graph', () => {
      createNode(experiment, 'A', [0], [0, 1, 2, 3]);
      createNode(experiment, 'B', [0], [0, 1]);
      createNode(experiment, 'C', [0, 1], [0, 1]);
      createNode(experiment, 'D', [0, 1, 2, 3], []);
      createNode(experiment, 'E', [0, 1], [0]);

      createEdge(experiment, 'A', 0, 'B', 0);
      createEdge(experiment, 'A', 1, 'C', 1);
      createEdge(experiment, 'A', 2, 'D', 2);
      createEdge(experiment, 'A', 3, 'E', 1);
      createEdge(experiment, 'B', 0, 'C', 0);
      createEdge(experiment, 'B', 1, 'D', 0);
      createEdge(experiment, 'C', 0, 'D', 1);
      createEdge(experiment, 'C', 1, 'E', 0);
      createEdge(experiment, 'E', 0, 'D', 3);

      expect(DeepsenseCycleAnalyser.cycleExists(experiment)).toBe(false);
    });


    it('which does not detect a cycle in more complex graph (reversed labels)', () => {
      createNode(experiment, 'A', [0, 1, 2, 3], []);
      createNode(experiment, 'B', [0, 1], [0]);
      createNode(experiment, 'C', [0, 1], [0, 1]);
      createNode(experiment, 'D', [0], [0, 1]);
      createNode(experiment, 'E', [0], [0, 1, 2, 3]);

      createEdge(experiment, 'B', 0, 'A', 3);
      createEdge(experiment, 'C', 0, 'A', 1);
      createEdge(experiment, 'C', 1, 'B', 0);
      createEdge(experiment, 'D', 1, 'A', 0);
      createEdge(experiment, 'D', 0, 'C', 0);
      createEdge(experiment, 'E', 0, 'D', 0);
      createEdge(experiment, 'E', 1, 'C', 1);
      createEdge(experiment, 'E', 2, 'A', 2);
      createEdge(experiment, 'E', 3, 'B', 1);

      expect(DeepsenseCycleAnalyser.cycleExists(experiment)).toBe(false);
    });

    it('which detects a cycle in a graph which has more than 1 components', () => {
      createNode(experiment, 'A', [0], [0]);
      createNode(experiment, 'B', [0], [0]);
      createNode(experiment, 'C', [0], [0]);
      createNode(experiment, 'D', [0], [0]);
      createNode(experiment, 'E', [0], [0]);
      createNode(experiment, 'F', [0], [0]);

      createEdge(experiment, 'A', 0, 'B', 0);
      createEdge(experiment, 'B', 0, 'C', 0);
      createEdge(experiment, 'D', 0, 'E', 0);
      createEdge(experiment, 'E', 0, 'F', 0);
      createEdge(experiment, 'F', 0, 'D', 0);

      expect(DeepsenseCycleAnalyser.cycleExists(experiment)).toBe(true);
    });
  });
});
