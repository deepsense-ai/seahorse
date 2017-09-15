const _ = require('lodash');
const es6Rules = require('./es6.rules');
const envRules = {
  env: {
    node: true
  },

  root: true,

  rules: {
    // {{{ ESLint

    // {{{ ESLint : Possible Errors
    'no-console': 'off'
    // ESLint : Possible Errors }}}

    // ESLint }}}
  }
};


module.exports = _.merge(es6Rules, envRules);
