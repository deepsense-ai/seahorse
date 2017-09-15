'use strict';

// Libs
import angular from 'angular';

// App
import EventEmitter from './event-emitter';
import * as Validators from './validators';

export default angular
  .module('common.helpers', [])
  .value('EventEmitter', EventEmitter)
  .value('Validators', Validators)
  .name;
