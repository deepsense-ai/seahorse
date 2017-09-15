'use strict';

/* @ngInject */
function MenuItemController($scope) {
  console.log('MenuItemController', this);
  console.log('Controllers scope', $scope);
}

exports.inject = function(module) {
  module.controller('MenuItemController', MenuItemController);
};
