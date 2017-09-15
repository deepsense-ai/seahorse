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
