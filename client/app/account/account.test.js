/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Account test', function() {
  var mod;

  mod = require('./account.module.js');

  beforeEach(function() {
    angular.mock.module('ui.router');
    angular.mock.module('ds.account');
  });

  describe('dataset list', function() {
    var ctrl;
    beforeEach(function() {
      angular.mock.inject(function($controller, $rootScope) {
        ctrl = $controller('Account');
      });
    });

    it('should have label', function() {
      expect(ctrl.label).toBe('Account info / settings page');
    });
  });
});
