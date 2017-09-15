/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var angular = require('angular');

var account = angular.module('ds.account', []);

require('./account.js').inject(account);
require('./account.config.js').inject(account);

module.exports = account;
