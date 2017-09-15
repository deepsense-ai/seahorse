/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Account test', function() {
  beforeEach(function() {
    let testModule = angular.module('test', ['ui.router']);

    angular.mock.module('test');
    angular.mock.module('ui.router');
    angular.mock.module('ds.account');

    require('../../app.run.js').inject(testModule);
    require('../../app.config.js').inject(testModule);
    require('../account.module.js');
    require('../../common/page.service.js').inject(testModule);
  });

  describe('account controller', function() {
    let PageService;
    let ctrl;

    beforeEach(angular.mock.inject(($injector, $controller) => {
      let $state = $injector.get('$state');
      $state.go('lab.account');

      ctrl = $controller('Account');
      PageService = $injector.get('PageService');
    }));

    it('should have label', function () {
      expect(ctrl.label).toBe('Account info / settings page');
    });

    it('should set nav title', function () {
      expect(PageService.getTitle()).toBe('My account');
    });
  });
});
