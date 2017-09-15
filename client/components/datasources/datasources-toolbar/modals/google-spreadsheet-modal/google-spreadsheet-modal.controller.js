'use strict';

import DatasourceModal from '../datasource-modal.class.js';

const GOOGLE_SPREADSHEET_REGEX = /[a-zA-Z0-9-_]+/;
const FULL_SPREADSHEET_URI_REGEX = /\/spreadsheets\/d\/([a-zA-Z0-9-_]+)/;

class GoogleSpreadsheetModalController extends DatasourceModal {
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

    $scope.$watch(() => this.datasourceParams.googleSpreadsheetParams.googleSpreadsheetId, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        const results = this.datasourceParams.googleSpreadsheetParams.googleSpreadsheetId.match(FULL_SPREADSHEET_URI_REGEX);
        if (results && results.length) {
          this.datasourceParams.googleSpreadsheetParams.googleSpreadsheetId = results[1];
        }
      }
    });
  }

  canAddDatasource() {
    return this.datasourceParams.name !== '' && this.isGoogleSpreadsheetIdValid() &&
      this.isGoogleCrendetialsValid() && !super.doesNameExists();
  }

  isGoogleSpreadsheetIdValid() {
    return !!this.datasourceParams.googleSpreadsheetParams.googleSpreadsheetId.match(GOOGLE_SPREADSHEET_REGEX);
  }

  isGoogleCrendetialsValid() {
    try {
      JSON.parse(this.datasourceParams.googleSpreadsheetParams.googleServiceAccountCredentials);
    } catch(e) {
      this.areCredentialsValid = false;
      return;
    }
    this.areCredentialsValid = true;
  }

}

export default GoogleSpreadsheetModalController;
