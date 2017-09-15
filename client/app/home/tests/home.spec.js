/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Home test', function () {
  beforeEach(() => {
    let testModule = angular.module('test', ['ui.router', 'toastr']);

    require('./../home.js').inject(testModule);
    require('./../home.config.js').inject(testModule);

    require('./../../app.run.js').inject(testModule);
    require('./../../app.config.js').inject(testModule);
    require('./../../common/services/page.service.js').inject(testModule);

    angular.mock.module('test');
  });

  describe('home controller', function () {
    let PageService;
    let ctrl;

    beforeEach(angular.mock.inject(($injector, $controller) => {
      let $state = $injector.get('$state');
      $state.go('lab.home');

      ctrl = $controller('Home');
      PageService = $injector.get('PageService');
    }));

    it('should say hello', function () {
      expect(ctrl.welcomeMessage).toBe('Hello! DeepSense.io engine at Your service!');
    });

    it('should set nav title', function () {
      expect(PageService.getTitle()).toBe('Home');
    });
  });
});
