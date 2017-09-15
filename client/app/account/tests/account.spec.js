/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Account test', function() {
  beforeEach(function() {
    let testModule = angular.module('test', []);

    angular.mock.module('test');
    angular.mock.module('ui.router');
    angular.mock.module('ds.account');

    require('../account.module.js');
    require('../../common/page.service.js').inject(testModule);
  });

  describe('account controller', function() {
    let PageService;
    let ctrl;

    beforeEach(angular.mock.inject(($injector, $controller) => {
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
