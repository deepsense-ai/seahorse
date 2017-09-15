/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

const spinner = angular.module('deepsense.spinner', []);

require('./lg/loading-spinner-lg.drv.js');
require('./processing/loading-spinner-processing.drv.js');
require('./sm/loading-spinner-sm.drv.js');

module.exports = spinner;
