/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
