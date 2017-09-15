/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

class ExperimentBrowserTopToolbar {
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/experiments/experiment-browser/experiment-browser-top-toolbar/experiment-browser-top-toolbar.html';
    this.replace = true;
    this.scope = {};
  }

  link(scope, element) {
    let addExperimentButton = element[0].querySelector('.add-experiment-button');

    addExperimentButton.addEventListener('click', () => {
      scope.$emit('Experiments.ADD_EXPERIMENT');
    });
  }

  static factory() {
    ExperimentBrowserTopToolbar.instance = new ExperimentBrowserTopToolbar();
    return ExperimentBrowserTopToolbar.instance;
  }
}

exports.inject = function (module) {
  module.directive('experimentBrowserTopToolbar', ExperimentBrowserTopToolbar.factory);
};
