'use strict';

import templateUrl from './datasources-list.html';
import './datasources-list.less';

const DatasourcesListComponent = {
  templateUrl,
  bindings: {
    datasources: '<'
  },
  controller: class DatasourcesListController {
    constructor() {
      'ngInject';

      this.datasources = [
        {
          id: '1',
          ownerId: 'owner',
          accessLevel: 'writeRead',
          creationDateTime: '01/01/10',
          params: {
            name: 'Datsource 1',
            visibility: 'publicVisibility',
            downloadUri: 'path/to/file1',
            datasourceType: 'jdbc',
            libraryFileParams: {
              libraryPath: 'library://path/to/file1',
              fileFormat: 'json'
            }
          }
        },
        {
          id: '2',
          ownerId: 'owner',
          accessLevel: 'read',
          creationDateTime: '01/01/10',
          params: {
            name: 'Datsource 2',
            visibility: 'privateVisibility',
            downloadUri: 'path/to/file2',
            datasourceType: 'libraryFile',
            libraryFileParams: {
              libraryPath: 'library://path/to/file2',
              fileFormat: 'json'
            }
          }
        }
      ];
    }

    sort(type, order) {

    }
  }
};

export default DatasourcesListComponent;
