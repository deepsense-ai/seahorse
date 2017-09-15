'use strict';

import PredefinedUser from './predefined-user';

const angular = require('angular');
const enums = angular.module('ds.enums', []);

enums.constant('PredefinedUser', PredefinedUser);

module.exports = enums;
