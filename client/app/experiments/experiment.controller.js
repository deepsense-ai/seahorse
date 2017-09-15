/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */

function ExperimentController($stateParams, $rootScope, Operations, DrawingService, ExperimentFactory, ExperimentAPIClient) {

  var that = this;
  var internal = {};

  var GraphNode = require('./common-objects/common-graph-node.js');
  var Edge = require('./common-objects/common-edge.js');

  internal.operations = null;
  internal.experiment = null;
  internal.selectedNode = null;

  internal.loadCatalog = () => {
    return Operations
      .getCatalog()
      .then((operationCatalog) => {
        console.log('Catalog downloaded successfully');
        that.operationsCatalog = operationCatalog;
      });
  };

  internal.loadOperations = () => {
    return Operations.getAll()
      .then((operations) => {
        console.log('Operations downloaded successfully');
        internal.operations = operations;
      });
  };

  internal.loadExperiment = () => {
    return ExperimentAPIClient
      .getData($stateParams.id)
      .then((data) => {
        console.log('Experiment downloaded successfully');
        $rootScope.headerTitle = 'Experiment: ' + data.experiment.name;
        internal.experiment = ExperimentFactory.createExperiment(data, internal.operations);
        DrawingService.renderExperiment(internal.experiment);
      });
  };

  internal.init = function init() {
    internal.loadCatalog()
      .then(internal.loadOperations)
      .then(internal.loadExperiment);
  };

  that.onRenderFinish = function onRenderFinish() {
    DrawingService.renderPorts();
    DrawingService.renderEdges();
    DrawingService.repaintEverything();
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
    return that.operationsCatalog;
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

  that.log = function log() {
    console.log(internal.experiment.getNodes());
    console.log(internal.experiment.getEdges());
  };


  $rootScope.$on(GraphNode.CLICK, (event, data) => {
    internal.selectedNode = data.selectedNode;
    $rootScope.$apply();
  });

  $rootScope.$on(GraphNode.MOVE, (data) => {
    that.saveData();
  });

  $rootScope.$on(Edge.CREATE, (data, args)  => {
    internal.experiment.addEdge(args.edge);
    $rootScope.$apply();
    that.saveData();
  });

  $rootScope.$on(Edge.REMOVE, (data, args)  => {
    console.log(args.edge);
    internal.experiment.removeEdge(args.edge);
    $rootScope.$apply();
    that.saveData();
  });

  $rootScope.$on('Keyboard.KEY_PRESSED', (event, data) => {
    if (internal.selectedNode) {
      internal.experiment.removeNode(internal.selectedNode.id);
      DrawingService.removeNode(internal.selectedNode.id);
      internal.selectedNode = null;
      that.onRenderFinish();
      $rootScope.$apply();
    }
  });

  $rootScope.$on('FlowChartBox.ELEMENT_DROPPED', function elementDropped(event, args) {
    var operation = that.getOperationById(args.classId);
    var moveX = args.dropEvent.x - args.target[0].getBoundingClientRect().left;
    var moveY = args.dropEvent.y - args.target[0].getBoundingClientRect().top;
    var offsetX = -100;
    var offsetY = -50;
    var node = internal.experiment.createNode(
      that.generateUUID(),
      operation,
      {},
      moveX + offsetX,
      moveY + offsetY
    );
    internal.experiment.addNode(node);
    DrawingService.repaintEverything();
    $rootScope.$apply();
    that.onRenderFinish();
    that.saveData();
  });

  $rootScope.$on('FlowChartBox.ELEMENT_DROPPED', ()=> that.log());
  $rootScope.$on('Keyboard.KEY_PRESSED', ()=> that.log());
  $rootScope.$on('Edge.REMOVE', ()=> that.log());

  internal.init();
  return that;
}

exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
};
