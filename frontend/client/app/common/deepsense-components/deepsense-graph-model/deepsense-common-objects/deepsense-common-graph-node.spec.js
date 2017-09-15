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

require('./deepsense-common-graph-node.js');

describe('graphNode', () => {
  let Edge;
  let GraphNode;
  let DeepsenseNodeParameters;
  let initId = '111-111-111';
  let initOperationId = '55-55-55';
  let initName = 'Sample operation';
  let initVersion = '1.1';

  let initData = {
    'id': initId,
    'operationId': initOperationId,
    'name': initName,
    'version': initVersion,
    'description': 'Aaaaa Bbbbb Cccc...',
    'input': [{
      'required': true,
      'portIndex': 0,
      'typeQualifier': ['smth']
    }],
    'output': [],
    'parametersValues': {}
  };
  let serializedData = {
    'id': initId,
    'operation': {
      'id': initOperationId,
      'name': initName,
      'version': initVersion
    },
    'parameters': {}
  };

  beforeEach(() => {
    angular.mock.module('deepsense.graph-model');
    angular.mock.inject((_GraphNode_, _DeepsenseNodeParameters_, _Edge_) => {
      GraphNode = _GraphNode_;
      DeepsenseNodeParameters = _DeepsenseNodeParameters_;
      Edge = _Edge_;
    });
  });

  it('should be defined', () => {
    expect(GraphNode).toBeDefined();
    expect(GraphNode).toEqual(jasmine.any(Function));
  });

  it('should have serialize method', () => {
    let graphNode = new GraphNode(initData);

    expect(graphNode.serialize).toEqual(jasmine.any(Function));
    expect(graphNode.serialize()).toEqual(serializedData);
  });

  it('should be able to set parameters', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.serialize()).toEqual(serializedData);

    graphNode.setParameters({}, DeepsenseNodeParameters);
    expect(graphNode.serialize()).toEqual(serializedData);
  });

  it('handle status changes', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.state).toBe(null);

    graphNode.updateState({
      'status': 'QUEUED'
    });
    expect(graphNode.state.status).toBe(graphNode.STATUS.QUEUED);

    graphNode.updateState();
    expect(graphNode.state).toBe(null);

    graphNode.updateState(false);
    expect(graphNode.state).toBe(null);

    graphNode.updateState({});
    expect(graphNode.state.status).toBe(graphNode.STATUS.DRAFT);

    graphNode.updateState({
      'status': 'x'
    });
    expect(graphNode.state.status).toBe(graphNode.STATUS.DRAFT);
  });

  it('handle state changes', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.state).toBe(null);

    let results = ['result-1', 'result-2'];
    graphNode.updateState({
      'status': 'COMPLETED',
      'results': results
    });

    expect(graphNode.state.status).toBe(graphNode.STATUS.COMPLETED);
    expect(graphNode.state.results).toEqual(results);

    expect(graphNode.getResult(0)).toEqual(results[0]);
    expect(graphNode.getResult(1)).toEqual(results[1]);
    expect(graphNode.getResult(2)).not.toBeDefined();
  });

  it('should return knowledge incoming to port index if edge exists', () => {

    let graphNode = new GraphNode(initData);

    let startNodeId = '11';
    let startPortId = 1;
    let endPortId = 2;
    let edge = new Edge({
      'startNodeId': startNodeId,
      'startPortId': startPortId,
      'endNodeId': graphNode.id,
      'endPortId': endPortId
    });
    let knowledgeFromPort0 = 'knowledge-from-port-0';
    let knowledgeFromPort1 = 'knowledge-from-port-1';

    let mockNodeGetter = (nodeId) => {
      if (nodeId === startNodeId) {
        return {
          output: [knowledgeFromPort0, knowledgeFromPort1]
        };
      }
    };

    graphNode.nodeGetter = mockNodeGetter;
    graphNode.edges[edge.id] = edge;

    expect(graphNode.getIncomingKnowledge(endPortId)).toEqual(knowledgeFromPort1);
  });

  it('should return undefined as incoming knowledge if edge does not exist', () => {
    let graphNode = new GraphNode(initData);
    expect(graphNode.getIncomingKnowledge(0)).not.toBeDefined();
  });
});
