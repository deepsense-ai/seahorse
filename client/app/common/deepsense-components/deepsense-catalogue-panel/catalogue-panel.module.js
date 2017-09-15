/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 10.06.15.
 */


const cataloguePanel = angular.module('deepsense-catalogue-panel', ['ui.bootstrap.tpls', 'ui.bootstrap']);

require('./catalogue-panel/catalogue-panel.js');
require('./catalogue-panel-operation/catalogue-panel-operation.js');
require('./catalogue-panel-operations-category/catalogue-panel-operations-category.js');

module.exports = cataloguePanel;
