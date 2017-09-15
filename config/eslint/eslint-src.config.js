const _ = require('lodash');
const es6Rules = require('./es6.rules');
const angularRules = require('./angular.rules');
const srcRules = {
  env: {
    browser: true,
    jasmine: true
  },

  globals: {
    _: true,        // Should be imported when necessary
    $: true,        // Should be imported when necessary
    exports: true,
    jsPlumb: true,  // Should be imported when necessary
    jQuery: true,   // Should be imported when necessary
    module: true,
    moment: true,   // Should be imported when necessary
    require: true   // Refactor to use ES6 import
  },

  parserOptions: {
    sourceType: 'module'
  },

  root: true
};


module.exports = _.merge(es6Rules, angularRules, srcRules);
