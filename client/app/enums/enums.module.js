'use strict';

import PredefinedUser from './predefined-user';

const angular = require('angular');
const enums = angular.module('ds.enums', []);

require('./session-status.js').inject(enums);
enums.constant('PredefinedUser', PredefinedUser);


module.exports = enums;
