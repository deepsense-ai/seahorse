'use strict';

import angular from 'angular';

import commonApi from './api/api.module';


export default angular
  .module('common', [
    commonApi
  ])
  .name;
