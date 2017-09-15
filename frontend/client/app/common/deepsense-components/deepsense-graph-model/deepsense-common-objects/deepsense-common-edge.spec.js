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

require('./deepsense-common-edge.js');

describe('Edge', () => {
  let Edge;
  let startNodeId = '11';
  let endNodeId = '2-3';
  let startPortId = 0;
  let endPortId = 1;
  let initData = {
    'startNodeId': startNodeId,
    'startPortId': startPortId,
    'endNodeId': endNodeId,
    'endPortId': endPortId
  };
  let serializedData = {
    'from': {
      'nodeId': startNodeId,
      'portIndex': startPortId
    },
    'to': {
      'nodeId': endNodeId,
      'portIndex': endPortId
    }
  };

  beforeEach(() => {
    angular.mock.module('deepsense.graph-model');
    angular.mock.inject((_Edge_) => {
      Edge = _Edge_;
    });
  });

  it('should be defined', () => {
    expect(Edge).toBeDefined();
    expect(Edge).toEqual(jasmine.any(Function));
  });

  it('should have generated id', () => {
    let edge = new Edge(initData);
    let id = `${startNodeId}#${startPortId}_${endNodeId}#${endPortId}`;
    expect(edge.id).toEqual(id);
  });

  it('should have serialize method', () => {
    let edge = new Edge(initData);
    expect(edge.serialize).toEqual(jasmine.any(Function));
    expect(edge.serialize()).toEqual(serializedData);
  });
});
