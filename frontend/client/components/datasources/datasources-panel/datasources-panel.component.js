'use strict';

import templateUrl from './datasources-panel.html';
import './datasources-panel.less';

import {datasourceContext} from 'APP/enums/datasources-context.js';

const DatasourcesPanelComponent = {
  bindings: {},
  templateUrl,
  controller: class DatasourcesPanelController {
    constructor($scope, datasourcesService, DatasourcesPanelService) {
      'ngInject';

      this.datasourcesService = datasourcesService;
      this.DatasourcesPanelService = DatasourcesPanelService;
      this.datasourcesService.fetchDatasources();

      $scope.$watch(() => datasourcesService.datasources, () => {
        this.datasources = datasourcesService.datasources;
      });

      $scope.$watch(() => DatasourcesPanelService.datasourcesContext, (newContext) => {
        this.context = newContext;
      });
    }

    onSelect(datasource) {
      if (this.context !== datasourceContext.BROWSE_DATASOURCE) {
        this.DatasourcesPanelService.onDatasourceSelectHandler(datasource.datasource);
      }
    }
  }
};

export default DatasourcesPanelComponent;
