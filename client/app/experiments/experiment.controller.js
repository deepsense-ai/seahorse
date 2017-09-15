/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentController(
  experiment,
  $http, $modal, $timeout, $scope,
  PageService, Operations, GraphPanelRendererService, ExperimentService, ExperimentAPIClient, UUIDGenerator
) {
  const RUN_STATE_CHECK_INTERVAL = 2000;

  var that = this;
  var internal = {};

  var GraphNode = require('./common-objects/common-graph-node.js');
  var Edge = require('./common-objects/common-edge.js');

  internal.selectedNode = null;

  /**
   * Loads all view-specific data.
   */
  internal.init = function init() {
    PageService.setTitle('Experiment: ' + experiment.experiment.name);

    ExperimentService.setExperiment(ExperimentService.createExperiment(experiment, Operations.getData()));
    GraphPanelRendererService.setExperiment(ExperimentService.getExperiment());

    internal.updateAndRerenderEdges(experiment);
  };

  /**
   * Handles experiment state change.
   *
   * @param {object} data
   */
  internal.handleExperimentStateChange = function handleExperimentStateChange(data) {
    if (ExperimentService.experimentIsSet()) {
      ExperimentService.getExperiment().updateState(data.experiment.state);
      that.checkExperimentState();
    }
  };

  internal.rerenderEdges = function rerenderEdges() {
    ExperimentService.updateEdgesStates();
    GraphPanelRendererService.changeEdgesPaintStyles();
  };

  /**
   * Updates edges' states and rerender all edges.
   *
   * @param {Object} data
   */
  internal.updateAndRerenderEdges = function updateAndRerenderEdges(data) {
    ExperimentService.updateTypeKnowledge(data);
    internal.rerenderEdges();
  };

  /**
   * Saves the experiment data by sending a request to the server.
   *
   * @returns {Promise}
   */
  internal.saveExperiment = function saveExperiment() {
    let serializedExperiment = ExperimentService.getExperiment().serialize();

    return ExperimentAPIClient.
      saveData({
        'experiment': serializedExperiment
      }).
      then((result) => {
        if (ExperimentService.experimentIsSet()) {
          internal.handleExperimentStateChange(result);
          internal.updateAndRerenderEdges(result);

          // TODO: compare sent data with response / update experiment if needed
        }
      });
  };

  /**
   * Loads experiment state data.
   */
  internal.loadExperimentState = function loadExperimentState() {
    if (ExperimentService.experimentIsSet()) {
      ExperimentAPIClient.
        getData(ExperimentService.getExperiment().getId()).
        then((data) => {
          if (ExperimentService.experimentIsSet()) {
            internal.handleExperimentStateChange(data);
            internal.updateAndRerenderEdges(data);
          }
        }, (error) => {
          console.error('experiment fetch state error', error);
        });
    }
  };

  /**
   * Executes when all nodes are rendered.
   * It triggers the jsPlumb drawing.
   */
  that.onRenderFinish = function onRenderFinish() {
    GraphPanelRendererService.init();
    GraphPanelRendererService.renderPorts();
    GraphPanelRendererService.renderEdges();
    GraphPanelRendererService.repaintEverything();

    that.checkExperimentState();

    $scope.$broadcast('Experiment.RENDER_FINISHED');
  };

  /**
   * Triggers experiment state check.
   */
  that.checkExperimentState = function checkExperimentState() {
    $timeout.cancel(internal.runStateTimeout);
    if (ExperimentService.getExperiment().isRunning()) {
      internal.runStateTimeout = $timeout(internal.loadExperimentState, RUN_STATE_CHECK_INTERVAL, false);
    }
  };

  that.getCatalog = Operations.getCatalog;

  that.getExperiment = ExperimentService.getExperiment;

  that.getSelectedNode = function getSelectedNode() {
    return internal.selectedNode;
  };

  that.unselectNode = function unselectNode() {
    internal.selectedNode = null;
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
    internal.saveExperiment();
  });

  $scope.$on(Edge.CREATE, (data, args)  => {
    ExperimentService.getExperiment().addEdge(args.edge);
    internal.rerenderEdges();
    internal.saveExperiment();
  });

  $scope.$on(Edge.REMOVE, (data, args)  => {
    ExperimentService.getExperiment().removeEdge(args.edge);
    internal.rerenderEdges();
    internal.saveExperiment();
  });

  $scope.$on('Keyboard.KEY_PRESSED', (event, data) => {
    if (internal.selectedNode) {
      ExperimentService.getExperiment().removeNode(internal.selectedNode.id);
      GraphPanelRendererService.removeNode(internal.selectedNode.id);
      that.unselectNode();

      internal.rerenderEdges();

      that.onRenderFinish();
      $scope.$digest();

      internal.saveExperiment();
    }
  });

  $scope.$on('FlowChartBox.ELEMENT_DROPPED', function elementDropped(event, args) {
    let operation = Operations.get(args.classId);
    let boxPosition = args.target[0].getBoundingClientRect();
    let positionX = (args.dropEvent.pageX - boxPosition.left - window.scrollX) || 0;
    let positionY = (args.dropEvent.pageY - boxPosition.top - window.scrollY) || 0;
    let offsetX = 100;
    let offsetY = 30;
    let node = ExperimentService.getExperiment().createNode({
        'id': UUIDGenerator.generateUUID(),
        'operation': operation,
        'x': positionX > offsetX ? positionX - offsetX : 0,
        'y': positionY > offsetY ? positionY - offsetY : 0
      });

    ExperimentService.getExperiment().addNode(node);
    GraphPanelRendererService.repaintEverything();
    $scope.$digest();
    that.onRenderFinish();
    internal.saveExperiment();
  });

  $scope.$on('Experiment.RUN', () => {
    ExperimentAPIClient.runExperiment(ExperimentService.getExperiment().getId()).then((data) => {
      internal.handleExperimentStateChange(data);
    }, (error) => {
      console.log('experiment launch error', error);
    });
  });

  $scope.$on('Experiment.ABORT', () => {
    ExperimentAPIClient.abortExperiment(ExperimentService.getExperiment().getId()).then((data) => {
      internal.handleExperimentStateChange(data);
    }, (error) => {
      console.log('experiment abort error', error);
    });
  });

  $scope.$on('$destroy', () => {
    $timeout.cancel(internal.runStateTimeout);
    ExperimentService.clearExperiment();
    GraphPanelRendererService.clearExperiment();
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
