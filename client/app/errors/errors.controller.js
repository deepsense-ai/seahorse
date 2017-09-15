'use strict';

/* @ngInject */
function ErrorController(config, $rootScope, $stateParams) {
  $rootScope.stateData.dataIsLoaded = true;
  $rootScope.showView = true;
  this.getConfig = () => config;
  this.getErrorDescription = () => $stateParams.errorMessage;
  this.getAPIVersion = () => config.apiVersion;
  this.getType = () => $stateParams.type;
  this.getLink = () => config.apiHost + '/' + config.urlApiVersion + '/' + $stateParams.type + 's/' + $stateParams.id + '/download';
}

exports.function = ErrorController;

exports.inject = function(module) {
  module.controller('ErrorController', ErrorController);
};
