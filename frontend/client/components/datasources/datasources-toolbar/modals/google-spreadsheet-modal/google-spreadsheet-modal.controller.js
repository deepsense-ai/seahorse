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


const GOOGLE_SPREADSHEET_REGEX = /[a-zA-Z0-9-_]+/;
const FULL_SPREADSHEET_URI_REGEX = /\/spreadsheets\/d\/([a-zA-Z0-9-_]+)/;


class GoogleSpreadsheetModalController extends DatasourceModal {
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

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
      this.isGoogleCrendetialsValid();
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
    return super.canAddDatasource() &&
      this.isGoogleSpreadsheetIdValid() &&
      this.areCredentialsValid;
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
