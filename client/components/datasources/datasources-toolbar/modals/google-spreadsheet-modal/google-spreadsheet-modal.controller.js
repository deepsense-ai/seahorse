'use strict';

import BaseDatasourceModalController from '../base-datasource-modal-controller.js';

class GoogleSpreadsheetModalController extends BaseDatasourceModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService, editedDatasource) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource);

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'googleSpreadsheet',
        googleSpreadsheetParams: {
          googleSpreadsheetId: '',
          googleServiceAccountCredentials: '',
          includeHeader: false,
          convert01ToBoolean: false
        }
      };
    }

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.canAddDatasource();
    }, true);
  }

  canAddDatasource() {
    return this.datasourceParams.googleSpreadsheetParams.googleSpreadsheetId !== '' &&
      this.datasourceParams.googleSpreadsheetParams.googleServiceAccountCredentials !== '' &&
      this.datasourceParams.name !== '' &&
      !super.doesNameExists();
  }
}

export default GoogleSpreadsheetModalController;
