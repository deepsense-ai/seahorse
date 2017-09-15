/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Datasets test', function() {
  var mod;

  mod = require('./datasets.module.js');

  beforeEach(function() {
    angular.mock.module('ui.router');
    angular.mock.module('ds.datasets');
  });

  describe('dataset list', function() {
    var ctrl;
    beforeEach(function() {
      angular.mock.inject(function($controller, $rootScope) {
        ctrl = $controller('DatasetList');
      });
    });

    it('should say you do not have any datasets', function() {
      expect(ctrl.setsLabel).toBe('You do not have any datasets!');
    });
  });
});
