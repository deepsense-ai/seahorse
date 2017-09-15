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

require('./../deepsense-graph-model/deepsense-graph-model.module.js');

const cycleAnalyser = angular
  .module('deepsense.cycle-analyser', ['deepsense.graph-model'])
  .factory('DeepsenseCycleAnalyser', /* @ngInject */function() {
    let cycleExists = (experiment) => {
      const COLOUR = {
        'WHITE': 'white',
        'GREY': 'grey',
        'BLACK': 'black'
      };
      let colour = {};
      let stack = [];
      let nodesIds = experiment.getNodesIds();
      let cycleDetected = false;
      let dfs = () => {
        while (stack.length > 0) {
          let currNodeId = _.last(stack);

          switch (colour[currNodeId]) {
            case COLOUR.WHITE: {
              colour[currNodeId] = COLOUR.GREY;

              let neighbourNodesIds = experiment.getNeightbours(currNodeId);
              for (let i = 0; i < neighbourNodesIds.length; i++) {
                let neighNodeId = neighbourNodesIds[i];
                if (colour[neighNodeId] === COLOUR.GREY) {
                  cycleDetected = true;
                }
                stack.push(neighNodeId);
              }

              break;
            }
            case COLOUR.GREY:
              colour[currNodeId] = COLOUR.BLACK;
              stack.pop();
              break;
            case COLOUR.BLACK:
              stack.pop();
              break;
            // no default
          }
        }
      };

      _.forEach(nodesIds, (nodeId) => {
        colour[nodeId] = COLOUR.WHITE;
      });

      _.forEach(nodesIds, (nodeId) => {
        if (colour[nodeId] === COLOUR.WHITE && !cycleDetected) {
          stack.push(nodeId);
          dfs();
        }
      });

      return cycleDetected;
    };

    return {
      cycleExists: cycleExists
    };
  });

module.exports = cycleAnalyser;
