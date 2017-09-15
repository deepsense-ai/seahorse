/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Home test', function() {
  var home;

  home = require('./home.module.js');

  beforeEach(function() {
    angular.mock.module('ui.router');
    angular.mock.module('ds.home');
  });

  describe('home controller', function() {
    var scope, ctrl;
    beforeEach(function() {
      angular.mock.inject(function($controller, $rootScope) {
        ctrl = $controller('Home');
      });
    });

    it('should say hello', function() {
      expect(ctrl.welcomeMessage).toBe('Hello!');
    });
  });
});
