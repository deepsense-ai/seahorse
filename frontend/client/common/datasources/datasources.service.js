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


export default class DatasourcesService {
  constructor(datasourcesApiService, UUIDGenerator, UserService, $log) {
    'ngInject';

    this.$log = $log;
    this.datasourcesApi = datasourcesApiService;
    this.uuid = UUIDGenerator;
    this.UserService = UserService;

    this.datasources = [];
  }


  addDatasource(params) {
    this.$log.info('DatasourcesService.addDatasource()', params);

    const datasourceId = this.uuid.generateUUID();

    return this.datasourcesApi
      .putDatasource(datasourceId, params)
      .then(() => {
        this.fetchDatasources();
      });
  }


  deleteDatasource(datasourceId) {
    this.$log.info(`DatasourcesService.deleteDatasource(${datasourceId})`);

    return this.datasourcesApi
      .deleteDatasource(datasourceId)
      .then(() => {
        this.fetchDatasources();
      });
  }


  fetchDatasources() {
    this.$log.info('DatasourcesService.fetchDatasources()');

    return this.datasourcesApi
      .getDatasources()
      .then((datasources) => {
        this.datasources = datasources;
      });
  }


  updateDatasource(datasource) {
    this.$log.info('DatasourcesService.updateDatasource()', datasource);

    return this.datasourcesApi
      .putDatasource(datasource.id, datasource.params)
      .then(() => {
        this.fetchDatasources();
      });
  }


  isCurrentUserOwnerOfDatasource(datasource) {
    return this.UserService.getSeahorseUser().id === datasource.ownerId;
  }


  isNameUsed(datasourceName) {
    return !!this.datasources.find((datasource) => {
      return datasource.params.name === datasourceName;
    });
  }

}
