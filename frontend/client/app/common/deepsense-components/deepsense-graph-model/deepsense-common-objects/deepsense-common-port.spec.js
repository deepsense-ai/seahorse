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

require('./deepsense-common-port.js');

describe('port', () => {
  let Port;
  let initNodeId = 'node1';
  let initType = 'portType';
  let initPortIndex = 1;
  let initData = {
    'nodeId': initNodeId,
    'type': initType,
    'portIndex': initPortIndex,
    'required': true,
    'typeQualifier': []
  };

  beforeEach(() => {
    angular.mock.module('deepsense.graph-model');
    angular.mock.inject((_Port_) => {
      Port = _Port_;
    });
  });

  it('should be defined', () => {
    expect(Port).toBeDefined();
    expect(Port).toEqual(jasmine.any(Function));
  });

  it('should have generated id', () => {
    let port = new Port(initData);
    let id = `${initType}-${initPortIndex}-${initNodeId}`;
    expect(port.id).toEqual(id);
  });
});
