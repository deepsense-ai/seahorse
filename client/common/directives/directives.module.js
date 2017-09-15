'use strict';

// Libs
import angular from 'angular';

// App
import onKbdEnterDirective from './on-kbd-enter.directive';
import onKbdEscDirective from './on-kbd-esc.directive';


export default angular
  .module('common.directives', [])
  .directive('onKbdEnter', onKbdEnterDirective)
  .directive('onKbdEsc', onKbdEscDirective)
  .name;
