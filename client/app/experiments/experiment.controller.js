/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */

function ExperimentController($timeout, $stateParams, $scope, PageService, Operations, DrawingService, ExperimentFactory, ExperimentAPIClient) {
  const RUN_STATE_CHECK_INTERVAL = 2000;

  var that = this;
  var internal = {};

  var GraphNode = require('./common-objects/common-graph-node.js');
  var Edge = require('./common-objects/common-edge.js');

  internal.operations = null;
  internal.experiment = null;
  internal.selectedNode = null;
  internal.isDataLoaded = false;

  internal.init = function init() {
    internal.initializeOperations().then(internal.loadExperiment);
  };

  internal.loadCatalog = () => {
    return Operations
      .getCatalog()
      .then((operationCatalog) => {
        console.log('Catalog downloaded successfully');
        that.operationsCatalog = operationCatalog;
      });
  };

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
        internal.experiment = ExperimentFactory.createExperiment(data, Operations.getData());
        DrawingService.renderExperiment(internal.experiment);
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
    internal.experiment.updateState(data.experiment.state);
    that.checkExperimentState();
  };

  /**
   * Loads experiment state data.
   */
  that.loadExperimentState = function loadExperimentState() {
    ExperimentAPIClient.getData(internal.experiment.getId()).then(that.handleExperimentStateChange,
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
    if (internal.experiment.isRunning()) {
      internal.runStateTimeout = $timeout(that.loadExperimentState, RUN_STATE_CHECK_INTERVAL, false);
    }
  };


  /**
   * Generates uuid part.
   *
   * @return {string}
   */
  var generateUUIDPart = function generateUUIDPart() {
    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  };

  /**
   * Generates uuid.
   *
   * @return {string}
   */
  that.generateUUID = function generateGUID() {
    return (
    generateUUIDPart() + generateUUIDPart() + '-' +
    generateUUIDPart() + '-' +
    generateUUIDPart() + '-' +
    generateUUIDPart() + '-' +
    generateUUIDPart() + generateUUIDPart() + generateUUIDPart()
    );
  };

  that.getCatalog = function getCatalog() {
    return internal.isDataLoaded ? Operations.getCatalog() : undefined;
  };

  that.getOperations = function getOperations() {
    return internal.operations;
  };

  that.getOperationById = function getOperationById(id) {
    return internal.operations[id];
  };

  that.getExperiment = function getExperiment() {
    return internal.experiment;
  };

  that.getParametersSchemaById = function getParametersSchemaById(id) {
    return internal.experiment.getParametersSchema()[id];
  };

  that.getSelectedNode = function getSelectedNode() {
    return internal.selectedNode;
  };

  that.unselectNode = function unselectNode() {
    internal.selectedNode = null;
  };

  that.saveData = function saveData() {
    let data = that.getExperiment().serialize();
    ExperimentAPIClient.saveData({
      'experiment': data
    }).then(() => {
      // TODO: compare sent data with response / update experiment if needed
    });
  };

  $scope.$on(GraphNode.CLICK, (event, data) => {
    internal.selectedNode = data.selectedNode;
    $scope.$digest();
  });

  $scope.$on(GraphNode.MOVE, (data) => {
    that.saveData();
  });

  $scope.$on(Edge.CREATE, (data, args)  => {
    internal.experiment.addEdge(args.edge);
    $scope.$digest();
    that.saveData();
  });

  $scope.$on(Edge.REMOVE, (data, args)  => {
    internal.experiment.removeEdge(args.edge);
    $scope.$digest();
    that.saveData();
  });

  $scope.$on('Keyboard.KEY_PRESSED', (event, data) => {
    if (internal.selectedNode) {
      internal.experiment.removeNode(internal.selectedNode.id);
      DrawingService.removeNode(internal.selectedNode.id);
      internal.selectedNode = null;
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
      node = internal.experiment.createNode({
        'id': that.generateUUID(),
        'operation': operation,
        'x': positionX > offsetX ? positionX - offsetX : 0,
        'y': positionY > offsetY ? positionY - offsetY : 0
      });
    internal.experiment.addNode(node);
    DrawingService.repaintEverything();
    $scope.$digest();
    that.onRenderFinish();
    that.saveData();
  });

  $scope.$on('Experiment.RUN', () => {
    ExperimentAPIClient.runExperiment(internal.experiment.getId()).then((data) => {
      that.handleExperimentStateChange(data);
    }, (error) => {
      console.log('experiment launch error', error);
    });
  });

  $scope.$on('Experiment.ABORT', () => {
    ExperimentAPIClient.abortExperiment(internal.experiment.getId()).then((data) => {
      that.handleExperimentStateChange(data);
    }, (error) => {
      console.log('experiment abort error', error);
    });
  });

  $scope.$on('$destroy', () => {
    $timeout.cancel(internal.runStateTimeout);
  });

  internal.init();
  return that;
}

exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
};
