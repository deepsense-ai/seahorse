/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

describe('Datasets test', function() {
  beforeEach(function() {
    let testModule = angular.module('test', ['ui.router']);

    angular.mock.module('test');
    angular.mock.module('ui.router');
    angular.mock.module('ds.datasets');

    require('../../app.run.js').inject(testModule);
    require('../../app.config.js').inject(testModule);
    require('../datasets.module.js');
    require('../../common/page.service.js').inject(testModule);
  });

  describe('dataset controller', function() {
    let PageService;
    let ctrl;

    beforeEach(angular.mock.inject(($injector, $controller) => {
      let $state = $injector.get('$state');
      $state.go('lab.datasets');

      ctrl = $controller('DatasetList');
      PageService = $injector.get('PageService');
    }));

    it('should say you do not have any datasets', function () {
      expect(ctrl.setsLabel).toBe('You do not have any datasets!');
    });

    it('should set nav title', function () {
      expect(PageService.getTitle()).toBe('Datasets');
    });
  });
});
