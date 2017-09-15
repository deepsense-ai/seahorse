/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

class LoadingSpinnerSm {
  constructor() {
    this.scope = {};
    this.replace = true;
    this.templateUrl = 'app/common/deepsense-components/deepsense-loading-spinner/sm/loading-spinner-sm.tpl.html';
  }

  static factory() {
    LoadingSpinnerSm.instance = new LoadingSpinnerSm();
    return LoadingSpinnerSm.instance;
  }
}

angular.module('deepsense.spinner').directive('deepsenseLoadingSpinnerSm', LoadingSpinnerSm.factory);
