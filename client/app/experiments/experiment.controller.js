/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function ExperimentController($scope,$stateParams, $rootScope, Operations, DrawingService, ExperimentFactory, ExperimentAPIClient) {

  var that = this;
  var internal = {};

  var GraphNode = require('./common-objects/common-graph-node.js'),
      Edge = require('./common-objects/common-edge.js');

  internal.operations = null;
  internal.experiment = null;
  internal.selectedNode = null;

  internal.init = function init() {
    console.log('Initiating experiment');

    Operations.getCatalog().then((data) => {
      console.log('Catalog downloaded successfully');
      that.operationsCatalog = data;
    });

    Operations.getAll()
      .then((data) => {
        console.log('Operations downloaded successfully');
        internal.operations = data;
      })
      .then(()=> {
        ExperimentAPIClient.getData($stateParams.id).then((data) => {
          console.log('Experiment downloaded successfully');
          $rootScope.headerTitle = 'Experiment: ' + data.experiment.name;
          internal.experiment = ExperimentFactory.createExperiment(data, internal.operations);
          DrawingService.renderExperiment(internal.experiment);
        });
      });
  };

  that.onRenderFinish = function onRenderFinish() {
    //console.log('Nodes rendering finished');
    DrawingService.renderPorts();
    DrawingService.renderConnections();
    jsPlumb.repaintEverything();
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

  $rootScope.$on(GraphNode.CLICK, (event, data) => {
    internal.selectedNode = data.selectedNode;
    $rootScope.$apply();
  });

  $rootScope.$on(GraphNode.MOVE, ()  => that.saveData());
  $rootScope.$on(Edge.CREATE, ()  => that.saveData());
  $rootScope.$on(Edge.REMOVE, ()  => that.saveData());

  $rootScope.$on('Keyboard.KEY_PRESSED', (event,data) => {
    DrawingService.removeNode(internal.selectedNode.id);
    console.log(internal.experiment.removeNode(internal.selectedNode.id));
    $rootScope.$apply();
    that.onRenderFinish();
    jsPlumb.repaintEverything();
  });

  $rootScope.$on('FlowChartBox.ELEMENT_DROPPED', function elementDropped(event, args) {
    var operation = that.getOperationById(args.classId);
    var moveX = args.dropEvent.x-args.target[0].getBoundingClientRect().left;
    var moveY = args.dropEvent.y-args.target[0].getBoundingClientRect().top;
    var offsetX = -100;
    var offsetY = -50;
    var node = internal.experiment.createNode(new Date().getTime().toString(),operation,{},moveX+offsetX,moveY+offsetY);
    internal.experiment.addNode(node);
    $rootScope.$apply();
    that.onRenderFinish();
    jsPlumb.repaintEverything();
    //that.saveData();
  });

  internal.init();
  return that;
}

exports.function = ExperimentController;

exports.inject = function (module) {
  module.controller('ExperimentController', ExperimentController);
};
