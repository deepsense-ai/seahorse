/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

class LoadingSpinnerLg {
  constructor() {
    this.scope = {};
    this.replace = true;
    this.templateUrl = 'lg/loading-spinner-lg.tpl.html';
  }

  static factory() {
    LoadingSpinnerLg.instance = new LoadingSpinnerLg();
    return LoadingSpinnerLg.instance;
  }
}

angular.module('deepsense.spinner').directive('deepsenseLoadingSpinnerLg', LoadingSpinnerLg.factory);