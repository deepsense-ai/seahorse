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
}
