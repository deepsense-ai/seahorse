'use strict';

const _ = require('lodash');
const _configs = {

  // global section
  global: require(__dirname + '/config/webpack/global'),

  // config by environments
  production: require(__dirname + '/config/webpack/production'),
  development: require(__dirname + '/config/webpack/development')
};

const _load = function() {
  const ENV = process.env.NODE_ENV
    ? process.env.NODE_ENV
    : 'production';

  console.log('Current Environment: ', ENV);

  // load config file by environment
  return _configs && _.merge(
      _configs.global(__dirname),
      _configs[ENV](__dirname)
    );
};

module.exports = _load();
