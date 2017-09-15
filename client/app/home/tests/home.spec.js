/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Home test', function () {
  beforeEach(() => {
    let testModule = angular.module('test', []);

    angular.mock.module('test');
    angular.mock.module('ui.router');
    angular.mock.module('ds.home');

    require('../home.module.js');
    require('../../common/page.service.js').inject(testModule);
  });

  describe('home controller', function () {
    let PageService;
    let ctrl;

    beforeEach(angular.mock.inject(($injector, $controller) => {
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
