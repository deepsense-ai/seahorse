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

// App
import DatasourceModal from '../datasource-modal.class.js';


class DatabaseModalController extends DatasourceModal {
  constructor(
    $scope,
    $log,
    $uibModalInstance,
    datasourcesService,
    editedDatasource,
    mode
  ) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource, mode);

    this.copyFromQueryInput = true;
    this.sqlInstruction = '';

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
      this.copyFromQueryInput = false;

      if (editedDatasource.params.jdbcParams.query) {
        this.type = 'query';
        this.sqlInstruction = editedDatasource.params.jdbcParams.query;
      } else if (editedDatasource.params.jdbcParams.table) {
        this.type = 'table';
        this.sqlInstruction = editedDatasource.params.jdbcParams.table;
      }
    } else {
      this.type = 'table';
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'jdbc',
        jdbcParams: {
          driver: '',
          url: '',
          query: null,
          table: null
        }
      };
    }

    $scope.$watch(() => this.sqlInstruction, (sqlInstruction) => {
      if (this.copyFromQueryInput) {
        this.datasourceParams.name = sqlInstruction;
      }
    });

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.canAddDatasource();
    }, true);
  }


  canAddDatasource() {
    return super.canAddDatasource() &&
      this.datasourceParams.jdbcParams.driver !== '' &&
      this.datasourceParams.jdbcParams.url !== '';
  }

  stopCopyingFromUserField() {
    this.copyFromQueryInput = false;
  }

  onQueryTypeChange() {
    if (this.type === 'table') {
      this.datasourceParams.jdbcParams.query = null;
      this.datasourceParams.jdbcParams.table = this.sqlInstruction;
    } else {
      this.datasourceParams.jdbcParams.query = this.sqlInstruction;
      this.datasourceParams.jdbcParams.table = null;
    }
  }
}

export default DatabaseModalController;
