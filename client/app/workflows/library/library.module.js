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
