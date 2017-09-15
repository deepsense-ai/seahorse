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
import {specialOperations} from 'APP/enums/special-operations.js';
import datasourceNodeTpl from './datasource-node.html';

export const datasourceNode = {
  templateUrl: datasourceNodeTpl,

  openDatasourcePanel() {
    if (!this.isEditable()) {
      return;
    }

    this.DatasourcesPanelService.setHandlerOnDatasourceSelect((datasource) => this.setDatasource(datasource));
    if (this.node.operationId === specialOperations.DATASOURCE.READ) {
      this.DatasourcesPanelService.openDatasourcesForReading();
    } else {
      this.DatasourcesPanelService.openDatasourcesForWriting();
    }
  },

  setDatasource(datasource) {
    this.DatasourcesPanelService.closeDatasources();
    this.node.parameters.parameters[0].value = datasource.id;
  },

  getDatasourceName() {
    const datasourceId = this.getDatasourceId();

    if (datasourceId) {
      const datasource = this.datasourcesService.datasources.find(datasource => datasource.id === datasourceId);
      return datasource ? datasource.params.name : 'Select data source';
    } else if (this.node.operationId === specialOperations.DATASOURCE.READ) {
      return 'Select data source';
    } else if (this.node.operationId === specialOperations.DATASOURCE.WRITE) {
      return 'Select emplacement';
    }
  },

  getDatasourceId() {
    if (this.node.parametersValues) {
      return this.node.parametersValues['data source'];
    } else if (this.node.parameters && this.node.parameters.parameters[0].value) {
      return this.node.parameters.parameters[0].value;
    } else {
      return '';
    }
  }
};

export default datasourceNode;
