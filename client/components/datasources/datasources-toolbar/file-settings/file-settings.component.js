'use strict';

import templateUrl from './file-settings.html';
import './file-settings.less';

const FileSettingsComponent = {
  bindings: {
    extension: '<',
    onChange: '&',
    fileSettings: '<',
    disabledMode: '<'
  },
  templateUrl,
  controller: class FileSettingsController {
    constructor($scope) {
      'ngInject';

      $scope.$watch(() => this.extension, (newValue, oldValue) => {
        if (newValue !== oldValue && newValue === 'csv') {
          this.fileSettings.csvFileFormatParams.includeHeader = true;
          this.fileSettings.csvFileFormatParams.convert01ToBoolean = false;
          this.fileSettings.csvFileFormatParams.separatorType = 'comma';
          this.onChange({fileSettings: this.fileSettings});
        }
      });

      $scope.$watch(() => this.fileSettings.csvFileFormatParams.separatorType, (newValue) => {
        if (newValue === 'custom') {
          document.querySelector('#custom-separator').focus();
        }
      });

      this.formats = ['csv', 'json', 'parquet'];
    }

    onFocus() {
      this.fileSettings.csvFileFormatParams.separatorType = 'custom';
      this.onChange({fileSettings: this.fileSettings});
    }
  }
};

export default FileSettingsComponent;
