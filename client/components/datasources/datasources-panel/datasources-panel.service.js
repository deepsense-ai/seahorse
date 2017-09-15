'use strict';

class DatasourcesPanelService {
  constructor() {
    'ngInject';

    this.isDatasourcesOpened = false;
  }

  openDatasources() {
    this.isDatasourcesOpened = true;
  }

  closeDatasources() {
    this.isDatasourcesOpened = false;
  }

}

export default DatasourcesPanelService;
