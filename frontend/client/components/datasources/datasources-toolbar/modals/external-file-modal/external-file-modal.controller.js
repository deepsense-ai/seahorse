'use strict';

// App
import DatasourceModal from '../datasource-modal.class.js';


const URL_REGEX = /(http|https|ftp):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\\.,@?^=%&amp;:/~\\+#]*[\w\-\\@?^=%&amp;/~\\+#])?/;


class ExternalFileModalController extends DatasourceModal {
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
      this.validateUrl();
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'externalFile',
        externalFileParams: {
          url: '',
          fileFormat: 'csv',
          csvFileFormatParams: {
            includeHeader: true,
            convert01ToBoolean: false,
            separatorType: 'comma',
            customSeparator: ''
          }
        }
      };
    }

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.canAddDatasource();
    }, true);
  }


  canAddDatasource() {
    const isCsvSeparatorValid = this.isCsvSeparatorValid(this.datasourceParams.externalFileParams);
    const isSourceValid = this.datasourceParams.externalFileParams.url !== '';

    return super.canAddDatasource() &&
      isCsvSeparatorValid &&
      isSourceValid;
  }


  validateUrl() {
    this.isUrlValid = this.datasourceParams.externalFileParams.url !== '' &&
        !!this.datasourceParams.externalFileParams.url.match(URL_REGEX);
  }


  onFileSettingsChange(newFileSettings) {
    this.datasourceParams.externalFileParams = Object.assign(
      {
        url: this.datasourceParams.externalFileParams.url
      },
      newFileSettings
    );
  }
}

export default ExternalFileModalController;
