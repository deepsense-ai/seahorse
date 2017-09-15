'use strict';

// App
import DatasourceModal from '../datasource-modal.class.js';


class LibraryModalController extends DatasourceModal {
  constructor(
    $scope,
    $log,
    $uibModalInstance,
    LibraryModalService,
    datasourcesService,
    DatasourcesPanelService,
    editedDatasource,
    previewMode
  ) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource, previewMode);

    this.LibraryModalService = LibraryModalService;
    this.DatasourcesPanelService = DatasourcesPanelService;

    this.$log.warn('LibraryModalController.constructor() datasourceParams:');
    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
      this.extension = this.getFileExtension(this.datasourceParams.libraryFileParams.libraryPath);

      this.$log.warn('Exists and equals to:', JSON.stringify(this.datasourceParams, null, 2));
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'libraryFile',
        libraryFileParams: {
          libraryPath: '',
          fileFormat: 'csv',
          csvFileFormatParams: {
            includeHeader: false,
            convert01ToBoolean: false,
            separatorType: '',
            customSeparator: ''
          }
        }
      };
      this.$log.warn('Does not exists and set to:', JSON.stringify(this.datasourceParams, null, 2));
    }

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.$log.warn('LibraryModalController $watch datasourceParams');
      this.$log.warn(JSON.stringify(newSettings, null, 2));

      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.canAddDatasource();
    }, true);

    if (!editedDatasource) {
      this.openLibrary();
    }
  }

  canAddDatasource() {
    const isCsvSeparatorValid = this.isCsvSeparatorValid(this.datasourceParams.libraryFileParams);
    const isSourceValid = this.datasourceParams.libraryFileParams.libraryPath !== '';

    return super.canAddDatasource() &&
      isCsvSeparatorValid &&
      isSourceValid;
  }

  openLibrary() {
    if (this.DatasourcesPanelService.isOpenedForWrite()) {
      this.LibraryModalService.openLibraryModal('write-to-file')
        .then((fullFilePath) => {
          if (fullFilePath) {
            this.setDatasourceParams(fullFilePath);
          } else if (!fullFilePath && !this.datasourceParams.libraryFileParams.libraryPath) {
            this.$uibModalInstance.close();
          }
        });
    } else {
      this.LibraryModalService.openLibraryModal('read-file')
        .then((file) => {
          if (file) {
            this.setDatasourceParams(file.uri);
          } else if (!file && !this.datasourceParams.libraryFileParams.libraryPath) {
            this.$uibModalInstance.close();
          }
        });
    }
  }


  getFileExtension(fullFilePath) {
    return fullFilePath
      .substr(fullFilePath.lastIndexOf('.') + 1)
      .toLowerCase();
  }

  setDatasourceParams(fullFilePath) {
    this.extension = this.getFileExtension(fullFilePath);
    this.datasourceParams.libraryFileParams.libraryPath = fullFilePath;
    this.datasourceParams.libraryFileParams.fileFormat = this.extension;
    this.datasourceParams.name = fullFilePath
      .split('.')
      .slice(0, fullFilePath.split('.').length - 1)
      .join('.')
      .replace('library://', '');

    this.$log.warn(`LibraryModalController.setDatasourceParams(${fullFilePath}) datasourceParams:`);
    this.$log.warn(JSON.stringify(this.datasourceParams, null, 2));
  }

  onFileSettingsChange(data) {
    this.$log.warn('LibraryModalController.onFileSettingsChange()');
    this.$log.warn('data is:', JSON.stringify(data, null, 2));

    this.datasourceParams.libraryFileParams = Object.assign({}, this.datasourceParams.libraryFileParams, data);
  }
}

export default LibraryModalController;
