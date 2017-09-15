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

require('./breadcrumbs.less');

const MAX_PARENT_NUMBER_VISIBLE = 4;

class BreadcrumbsController {
  /* @ngInject */
  constructor($scope, LibraryService) {
    this.LibraryService = LibraryService;

    $scope.$watch(() => this.allParents, (parents) => {
      this.shortenParentsArray(parents);
    });
  }

  goToParent(uri) {
    this.LibraryService.changeDirectory(uri);
  }

  shortenParentsArray(parents) {
    if (parents.length > MAX_PARENT_NUMBER_VISIBLE) {
      const firstParentNotVisible = parents[parents.length - MAX_PARENT_NUMBER_VISIBLE];
      this.parents = [
        parents[0],
        {
          name: '...',
          uri: firstParentNotVisible.uri,
          title: firstParentNotVisible.name
        },
        ...parents.slice(parents.length - (MAX_PARENT_NUMBER_VISIBLE - 1), parents.length)
      ];
    } else {
      this.parents = [...parents];
    }
  }

}

export default BreadcrumbsController;
