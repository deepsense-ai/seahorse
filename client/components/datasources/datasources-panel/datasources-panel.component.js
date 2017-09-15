'use strict';

import templateUrl from './datasources-panel.html';
import './datasources-panel.less';

const DatasourcesPanelComponent = {
  bindings: {},
  templateUrl,
  controller: class DatasourcesPanelController {
    constructor(datasourcesService, $scope) {
      'ngInject';

      this.datasourcesService = datasourcesService;
      this.datasourcesService.fetchDatasources();

      $scope.$watch(() => datasourcesService.datasources, () => {
        this.datasources = datasourcesService.datasources;
      });
    }

  }
};

export default DatasourcesPanelComponent;
