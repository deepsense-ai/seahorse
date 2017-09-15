import angular from 'angular';
import breadcrumbs from './breadcrumbs/breadcrumbs.component.js';
import FileList from './file-list/file-list.component.js';
import FileElement from './file-list/file-element/file-element.component.js';

const Library = angular
  .module('library', [])
  .component('breadcrumbs', breadcrumbs)
  .component('fileList', FileList)
  .component('fileElement', FileElement)
  .name;

export default Library;
