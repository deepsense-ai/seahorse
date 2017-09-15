/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 24.06.15.
 */

'use strict';

function FreezeService ($document, ExperimentService) {
  var that = this;
  var internal = {};

  internal.frozen = false;

  that.getState = function getState() {
    return internal.frozen;
  };

  that.toggleFreeze = function toggleFreeze (changeTo = false) {
    _.forEach(jsPlumb.getConnections(), connection => {
      connection.setDetachable(changeTo);
    });

    _.forEach(ExperimentService.getExperiment().getNodes(), node => {
      var element = $document[0].getElementById(`node-${node.id}`);

      _.forEach(jsPlumb.getEndpoints(element), endPoint => {
        endPoint.connectionsDetachable = changeTo;

        if (endPoint.connections.length === 0) {
          endPoint.setEnabled(changeTo);
        }
      });
    });

    internal.frozen = !changeTo;
  };

  that.unFreezeAllEndPoints = function unFreezeAllEndPoints () {
    that.toggleFreeze(true);
  };

  that.handleExperimentStateChange = function handleExperimentStateChange (state) {
    if (state === ExperimentService.getExperiment().STATUS.RUNNING) {
      that.toggleFreeze();
    } else {
      that.unFreezeAllEndPoints();
    }
  };
}

exports.function = FreezeService;

exports.inject = function (module) {
  module.service('FreezeService', FreezeService);
};
