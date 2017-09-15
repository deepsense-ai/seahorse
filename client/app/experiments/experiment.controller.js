/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentController(
  $http, $modal, $timeout, $stateParams, $scope,
  PageService, Operations, DrawingService, ExperimentService, ExperimentAPIClient, UUIDGenerator
) {
  const RUN_STATE_CHECK_INTERVAL = 2000;

  var that = this;
  var internal = {};

  var GraphNode = require('./common-objects/common-graph-node.js');
  var Edge = require('./common-objects/common-edge.js');

  internal.selectedNode = null;
  internal.isDataLoaded = false;

  internal.initializeOperations = () => {
    return Operations.load().then(() => {
      console.log('Operations data downloaded successfully');
    });
  };

  internal.loadExperiment = () => {
    return ExperimentAPIClient
      .getData($stateParams.id)
      .then((data) => {
        console.log('Experiment downloaded successfully');
        PageService.setTitle('Experiment: ' + data.experiment.name);
        ExperimentService.setExperiment(ExperimentService.createExperiment(data, Operations.getData()));
        DrawingService.renderExperiment(ExperimentService.getExperiment());
        internal.isDataLoaded = true;
      });
  };

  internal.init = function init() {
    internal.initializeOperations().then(internal.loadExperiment);
  };

  that.onRenderFinish = function onRenderFinish() {
    DrawingService.init();
    DrawingService.renderPorts();
    DrawingService.renderEdges();
    DrawingService.repaintEverything();
    that.checkExperimentState();
  };

  /**
   * Handles experiment state change.
   *
   * @param {object} data
   */
  that.handleExperimentStateChange = function handleExperimentStateChange(data) {
    ExperimentService.getExperiment().updateState(data.experiment.state);
    that.checkExperimentState();
  };

  /**
   * Loads experiment state data.
   */
  that.loadExperimentState = function loadExperimentState() {
    ExperimentAPIClient.getData(ExperimentService.getExperiment().getId()).then(that.handleExperimentStateChange,
      (error) => {
        console.error('experiment fetch state error', error);
      }
    );
  };

  /**
   * Triggers experiment state check.
   */
  that.checkExperimentState = function checkExperimentState() {
    $timeout.cancel(internal.runStateTimeout);
    if (ExperimentService.getExperiment().isRunning()) {
      internal.runStateTimeout = $timeout(that.loadExperimentState, RUN_STATE_CHECK_INTERVAL, false);
    }
  };

  that.getCatalog = function getCatalog() {
    return internal.isDataLoaded ? Operations.getCatalog() : undefined;
  };

  that.getExperiment = ExperimentService.getExperiment;

  that.getSelectedNode = function getSelectedNode() {
    return internal.selectedNode;
  };

  that.unselectNode = function unselectNode() {
    internal.selectedNode = null;
  };

  that.saveData = function saveData() {
    let data = ExperimentService.getExperiment().serialize();
    ExperimentAPIClient.saveData({
      'experiment': data
    }).then((result) => {
      that.handleExperimentStateChange(result);
      // TODO: compare sent data with response / update experiment if needed
    });
  };

  $scope.$on(GraphNode.CLICK, (event, data) => {
    let node = data.selectedNode;

    internal.selectedNode = node;

    if (node.hasParameters()) {
      $scope.$digest();
    } else {
      Operations.getWithParams(node.operationId).then((operationData) => {
        $scope.$applyAsync(() => {
          node.setParameters(operationData.parameters);
        });
      }, (error) => {
        console.error('operation fetch error', error);
      });
    }
  });

  $scope.$on(GraphNode.MOVE, (data) => {
    that.saveData();
  });

  $scope.$on(Edge.CREATE, (data, args)  => {
    ExperimentService.getExperiment().addEdge(args.edge);
    $scope.$digest();
    that.saveData();
  });

  $scope.$on(Edge.REMOVE, (data, args)  => {
    ExperimentService.getExperiment().removeEdge(args.edge);
    $scope.$digest();
    that.saveData();
  });

  $scope.$on('Keyboard.KEY_PRESSED', (event, data) => {
    if (internal.selectedNode) {
      ExperimentService.getExperiment().removeNode(internal.selectedNode.id);
      DrawingService.removeNode(internal.selectedNode.id);
      that.unselectNode();
      that.onRenderFinish();
      $scope.$digest();
      that.saveData();
    }
  });

  $scope.$on('FlowChartBox.ELEMENT_DROPPED', function elementDropped(event, args) {
    let operation = Operations.get(args.classId),
      boxPosition = args.target[0].getBoundingClientRect(),
      positionX = (args.dropEvent.pageX - boxPosition.left - window.scrollX) || 0,
      positionY = (args.dropEvent.pageY - boxPosition.top - window.scrollY) || 0,
      offsetX = 100,
      offsetY = 30,
      node = ExperimentService.getExperiment().createNode({
        'id': UUIDGenerator.generateUUID(),
        'operation': operation,
        'x': positionX > offsetX ? positionX - offsetX : 0,
        'y': positionY > offsetY ? positionY - offsetY : 0
      });
    ExperimentService.getExperiment().addNode(node);
    DrawingService.repaintEverything();
    $scope.$digest();
    that.onRenderFinish();
    that.saveData();
  });

  $scope.$on('Experiment.RUN', () => {
    ExperimentAPIClient.runExperiment(ExperimentService.getExperiment().getId()).then((data) => {
      that.handleExperimentStateChange(data);
    }, (error) => {
      console.log('experiment launch error', error);
    });
  });

  $scope.$on('Experiment.ABORT', () => {
    ExperimentAPIClient.abortExperiment(ExperimentService.getExperiment().getId()).then((data) => {
      that.handleExperimentStateChange(data);
    }, (error) => {
      console.log('experiment abort error', error);
    });
  });

  $scope.$on('$destroy', () => {
    $timeout.cancel(internal.runStateTimeout);
  });


  // --- MOCK ---
  // TODO: remove when it won't be needed anymore
  $scope.$on('Model.DEPLOY', (event, data) => {
    console.log('catching Model.DEPLOY');
    $modal.open({
      template: '<div class="inmodal"><div class="modal-header"><h4 class="modal-title">Deploy model</h4></div><div class="modal-body" style="min-height:75px;"><loading-spinner-sm ng-if="!linkValue && !error" style="font-size:10px;position:relative;"></loading-spinner-sm><div ng-hide="linkValue===undefined"><p>GET: <input type="text" class="form-control" value="{{::linkValue}}" ng-focus="linkValue!==undefined" readonly="true"></p><p>sample content:<textarea readonly="true" style="width:100%;height:75px;font-size:14px;padding:6px 12px;resize:none;background-color:#eee;border:1px solid #E5E6E7;">{'+'\n'+'    "features" : [1.1, 1.9, 5.7, 7.7]'+'\n'+'}</textarea></p><p>sample result:<textarea readonly="true" style="width:100%;height:75px;font-size:14px;padding:6px 12px;resize:none;background-color:#eee;border:1px solid #E5E6E7;">{'+'\n'+'    "score": 0.1'+'\n'+'}</textarea></p></div><p ng-hide="error!==true" style="color:red;margin-top:10px;">Error occurred while deploying model!</p></div><div class="modal-footer"><button type="button" class="btn btn-white" ng-click="close()">Close</button></div></div>',
      controller: ($scope, $modalInstance) => {
        $scope.close = function () {
          $modalInstance.close();
        };
        $http.get('/api/models/' + data.id + '/deploy').then((response) => {
          $scope.linkValue = response.data.link;
        }, (error) => {
          console.error('deploy api call error', error);
          $scope.error = true;
        });
      }
    });
  });
  // --- --- ---

  internal.init();
  return that;
}

exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
};
