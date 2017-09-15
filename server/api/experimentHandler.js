/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global console*/
'use strict';


module.exports = function experimentHandler(config) {
  var Q = require('q'),
      experimentModel = config.localDB.collections.experiment;

  /**
   * Filters UI data from body request.
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
      for (let nodeId in body.experiment.graph.nodes) {
        data.nodes[nodeId] = body.experiment.graph.nodes[nodeId].ui;
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
      if (!local) {
        console.error('local data is missing');
      }
      return data;
    }
    for (let nodeId in local.nodes) {
      data.experiment.graph.nodes[nodeId].ui = local.nodes[nodeId];
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
      console.log('create experiment');
      experimentModel.create({id: data.experiment.id}).then((result) => {
        deferred.resolve(data);
      }).catch((error) => {
        deferred.reject(error);
      });

    } else if (id.length) {
      switch (request.method) {

        case 'GET':
          console.log('get experiment', id);
          experimentModel.findOne().where({id: id}).then((result) => {
            deferred.resolve(mergeData(data, result));
          }).catch((error) => {
            deferred.reject(error);
          });
          break;

        case 'PUT':
          console.log('update experient', id);
          experimentModel.update().where({id: id}).set(getInputData(request.body))
          .then((result) => {
            console.log(result);
            deferred.resolve(mergeData(data, result[0]));
          }).catch((error) => {
            deferred.reject(error);
          });
          break;

        case 'DELETE':
          console.log('delete experiment', id);
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
