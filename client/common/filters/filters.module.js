'use strict';

// Libs
import angular from 'angular';

// App
import reverseFilter from './reverse.filter';


export default angular
  .module('common.filters', [])
  .filter('reverse', reverseFilter)
  .name;
