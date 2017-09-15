/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

import tpl from './loading-spinner-sm.tpl.html';

class LoadingSpinnerSm {
  constructor() {
    this.scope = {};
    this.replace = true;
    this.templateUrl = tpl;
  }

  static factory() {
    LoadingSpinnerSm.instance = new LoadingSpinnerSm();
    return LoadingSpinnerSm.instance;
  }
}

angular.module('deepsense.spinner').directive('deepsenseLoadingSpinnerSm', LoadingSpinnerSm.factory);
