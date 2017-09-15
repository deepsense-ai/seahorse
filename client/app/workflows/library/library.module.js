import angular from 'angular';
import breadcrumbs from './breadcrumbs/breadcrumbs.component.js';

const Library = angular
  .module('library', [])
  .component('breadcrumbs', breadcrumbs)
  .name;

export default Library;
