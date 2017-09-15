'use strict';

// Assets
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
    constructor($scope, $log) {
      'ngInject';

      this.$scope = $scope;
      this.$log = $log;

      this.$log.warn(`FileSettingsComponent extension is '${this.extension}'`);

      this.$scope.$watch(() => this.extension, (newValue, oldValue) => {
        this.$log.warn(`FileSettingsComponent $watch extension ${oldValue}-> ${newValue}`);
        this.$log.warn('fileSettings equals to:', JSON.stringify(this.fileSettings, null, 2));

        if (newValue !== oldValue && newValue === 'csv') {
          this.fileSettings.csvFileFormatParams.includeHeader = true;
          this.fileSettings.csvFileFormatParams.convert01ToBoolean = false;
          this.fileSettings.csvFileFormatParams.separatorType = 'comma';
          this.$log.warn('Update csv settings to', JSON.stringify(this.fileSettings, null, 2));
          this.onChange({fileSettings: this.fileSettings});
        }
      });

      this.formats = ['csv', 'json', 'parquet'];
    }


    $postLink() {
      // TODO: what if we change file type?
      if (this.fileSettings.fileFormat === 'csv') {
        this.$scope.$watch(() => this.fileSettings.csvFileFormatParams.separatorType, (newValue) => {
          if (newValue === 'custom') {
            document.querySelector('#custom-separator').focus();
          }
        });
      }
    }


    onFocus() {
      this.fileSettings.csvFileFormatParams.separatorType = 'custom';
      this.onChange({fileSettings: this.fileSettings});
    }
  }
};

export default FileSettingsComponent;
