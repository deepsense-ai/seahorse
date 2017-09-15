/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Grzegorz Swatowski on 05.08.15.
 */

'use strict';

angular.module('deepsense.cycle-analyser', ['deepsense.graph-model']).
  factory('DeepsenseCycleAnalyser', /*@ngInject*/function() {
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
            case COLOUR.WHITE:
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
            case COLOUR.GREY:
              colour[currNodeId] = COLOUR.BLACK;
              stack.pop();
              break;
            case COLOUR.BLACK:
              stack.pop();
              break;
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
