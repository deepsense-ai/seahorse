'use strict';

import footerTpl from './modal-footer/modal-footer.html';

class DatasourceModal {
  constructor($log, $uibModalInstance, datasourcesService, editedDatasource) {
    'ngInject';

    this.$log = $log;
    this.$uibModalInstance = $uibModalInstance;
    this.datasourcesService = datasourcesService;
    this.editedDatasource = angular.copy(editedDatasource);

    this.footerTpl = footerTpl;
  }

  isSeparatorValid(separatorType, customSeparator) {
    if (this.extension === 'csv') {
      if (separatorType) {
        return separatorType === 'custom' ? customSeparator !== '' : true;
      } else {
        return false;
      }
    }
    return true;
  }

  doesNameExists() {
    if (this.editedDatasource && this.editedDatasource.params.name === this.datasourceParams.name) {
      return false;
    } else {
      return this.datasourcesService.isNameUsed(this.datasourceParams.name);
    }
  }

  cancel() {
    this.$uibModalInstance.dismiss();
  }

  ok() {
    if (this.originalDatasource) {
      const params = this.datasourceParams;
      const updatedDatasource = Object.assign({}, this.originalDatasource, params);

      this.datasourcesService.updateDatasource(updatedDatasource)
        .then((result) => {
          this.$log.info('BaseDatasourceModal updateDatasource result', result);
          this.$uibModalInstance.close();
        })
        .catch((error) => {
          this.$log.info('BaseDatasourceModal updateDatasource error ', error);
        });
    } else {
      this.datasourcesService.addDatasource(this.datasourceParams)
        .then((result) => {
          this.$log.info('BaseDatasourceModal addDatasource result ', result);
          this.$uibModalInstance.close();
        })
        .catch((error) => {
          this.$log.info('BaseDatasourceModal addDatasource error ', error);
        });
    }
  }

}

export default DatasourceModal;
