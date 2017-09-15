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

import angular from 'angular';
import Breadcrumbs from './breadcrumbs/breadcrumbs.component';
import FileList from './file-list/file-list.component';
import FileElement from './file-list/file-element/file-element.component';
import RecentFilesIndicator from './recent-files-indicator/recent-files-indicator.component';
import FileUploadSection from './file-upload-section/file-upload-section.component';

const Library = angular
  .module('library', [])
  .component('breadcrumbs', Breadcrumbs)
  .component('fileList', FileList)
  .component('fileElement', FileElement)
  .component('recentFilesIndicator', RecentFilesIndicator)
  .component('fileUploadSection', FileUploadSection)
  .name;

export default Library;
