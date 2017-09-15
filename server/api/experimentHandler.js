/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


module.exports = function experimentHandler(config) {
  var Q = require('q'),
      experimentModel = config.localDB.collections.experiment;

  /**
   * Filters UI data from request body.
   *
   * @param {JSON} body
   *
   * @return {JSON}
   */
  var getInputData = function getInputData(body) {
    let data = {
      'nodes': {}
    };

    if (body.experiment.graph && body.experiment.graph.nodes) {
      let graphNodes = body.experiment.graph.nodes;
      for (let i = graphNodes.length - 1; i >= 0; i--) {
        let node = graphNodes[i];
        data.nodes[node.id] = node.ui;
      }
    }

    return data;
  };

  /**
   * Merges UI data with API response data.
   *
   * @param {JSON} data
   * @param {JSON} local
   *
   * @return {JSON}
   */
  var mergeData = function mergeData(data, local) {
    if (!local || !local.nodes) {
      local = {
        'nodes': {}
      };
    }

    let graphNodes = data.experiment.graph.nodes;
    for (let i = graphNodes.length - 1; i >= 0; i--) {
      let graphNode = graphNodes[i],
        localNode = local.nodes[graphNode.id];
      graphNode.ui = localNode || {
        'x': 0,
        'y': 0
      };
    }

    return data;
  };

  /**
   * API proxy handler.
   *
   * @param {JSON} data
   * @param {object} request
   *
   * @return {Promise}
   */
  var handler = function handler(data, request) {
    var deferred = Q.defer(),
        id = request.resourcePath;

    if (!experimentModel) {
      deferred.reject({'message': 'model not ready'});
    }

    if (!id && request.method === 'POST') {
      console.log('log:', 'create experiment');
      experimentModel.create({id: data.experiment.id}).then((result) => {
        deferred.resolve(data);
      }).catch((error) => {
        deferred.reject(error);
      });

    } else if (id.length) {
      switch (request.method) {

        case 'GET':
          console.log('log:', 'get experiment', id);
          experimentModel.findOne().where({id: id}).then((result) => {
            if (!result) {
              console.log('log:', 'missing local data - recovering');
              experimentModel.create({id: id}).then(() => {});
            }
            deferred.resolve(mergeData(data, result));
          }).catch((error) => {
            deferred.reject(error);
          });
          break;

        case 'PUT':
          console.log('log:', 'update experiment', id);
          experimentModel.update().where({id: id}).set(getInputData(request.body))
          .then((result) => {
            deferred.resolve(mergeData(data, result[0]));
          }).catch((error) => {
            deferred.reject(error);
          });
          break;

        case 'DELETE':
          console.log('log:', 'delete experiment', id);
          experimentModel.destroy().where({id: id}).then((result) => {
            deferred.resolve(data);
          }).catch((error) => {
            deferred.reject(error);
          });
          break;

        default:
          deferred.resolve(data);
      }

    } else {
      deferred.resolve(data);
    }

    return deferred.promise;
  };


  config.resources.experiments.handler = handler;
  return config;
};
