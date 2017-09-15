'use strict';

// Libs
import angular from 'angular';

// Assets
import templateUrl from './file-settings.html';
import './file-settings.less';


const DEFAULT_CSS_FILE_FORMAT_PARAMS = {
  includeHeader: true,
  convert01ToBoolean: false,
  separatorType: 'comma',
  customSeparator: ''
};


const FileSettingsComponent = {
  bindings: {
    onChange: '&',
    fileSettings: '<',
    disabledMode: '<'
  },

  templateUrl,

  controller: class FileSettingsController {
    constructor($scope, $log) {
      'ngInject';

      this.$scope = $scope;
      this.$log = $log;
      this.formats = ['csv', 'json', 'parquet'];
    }


    $onChanges(changed) {
      if (changed.fileSettings) {
        const newFileSettings = changed.fileSettings.currentValue;

        this.fileFormat = newFileSettings.fileFormat;
        this.csvFileFormatParams = angular.copy(newFileSettings.csvFileFormatParams || DEFAULT_CSS_FILE_FORMAT_PARAMS);
      }
    }


    $postLink() {
      if (this.fileFormat === 'csv') {
        this.$scope.$watch(() => this.csvFileFormatParams.separatorType, (newValue) => {
          if (newValue === 'custom') {
            document.querySelector('#custom-separator').focus();
          }
        });
      }
    }


    onCustomSeparatorInputFocus() {
      this.fileSettings.csvFileFormatParams.separatorType = 'custom';
      this.updateFileSettings();
    }


    updateFileSettings() {
      const fileSettings = {
        fileFormat: this.fileFormat
      };

      if (this.fileFormat === 'csv') {
        fileSettings.csvFileFormatParams = this.csvFileFormatParams;
        if (fileSettings.csvFileFormatParams.separatorType !== 'custom') {
          fileSettings.csvFileFormatParams.customSeparator = '';
        }
      }

      this.onChange({fileSettings});
    }
  }
};

export default FileSettingsComponent;
