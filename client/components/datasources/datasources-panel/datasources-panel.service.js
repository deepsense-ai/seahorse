'use strict';

import {datasourceContext} from 'APP/enums/datasources-context.js';

class DatasourcesPanelService {
  constructor() {
    'ngInject';

    this.isDatasourcesOpened = false;
  }

  openDatasourcesForBrowsing() {
    this.datasourcesContext = datasourceContext.BROWSE_DATASOURCE;
    this.isDatasourcesOpened = true;
  }

  openDatasourcesForReading() {
    this.datasourcesContext = datasourceContext.READ_DATASOURCE;
    this.isDatasourcesOpened = true;
  }

  openDatasourcesForWriting() {
    this.datasourcesContext = datasourceContext.WRITE_DATASOURCE;
    this.isDatasourcesOpened = true;
  }

  closeDatasources() {
    this.datasourcesContext = datasourceContext.BROWSE_DATASOURCE;
    this.isDatasourcesOpened = false;
  }

  setHandlerOnDatasourceSelect(fn) {
    this.onDatasourceSelectHandler = fn;
  }

  isOpenedForRead() {
    return this.datasourcesContext === datasourceContext.READ_DATASOURCE;
  }

  isOpenedForWrite() {
    return this.datasourcesContext === datasourceContext.WRITE_DATASOURCE;
  }

}

export default DatasourcesPanelService;
