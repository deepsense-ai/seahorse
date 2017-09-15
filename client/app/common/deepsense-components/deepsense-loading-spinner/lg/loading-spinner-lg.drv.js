/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

import tpl from './loading-spinner-lg.tpl.html';

class LoadingSpinnerLg {
  constructor() {
    this.scope = {};
    this.replace = true;
    this.templateUrl = tpl;
  }

  static factory() {
    LoadingSpinnerLg.instance = new LoadingSpinnerLg();
    return LoadingSpinnerLg.instance;
  }
}

angular.module('deepsense.spinner').directive('deepsenseLoadingSpinnerLg', LoadingSpinnerLg.factory);
