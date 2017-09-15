'use strict';

function cut() {
  return function(value, wordwise, max, tail) {
    if (!value) return '';

    max = parseInt(max, 10);
    if (!max || value.length <= max) {
      return value;
    }

    value = value.substr(0, max);
    if (wordwise) {
      var lastspace = value.lastIndexOf(' ');
      if (lastspace != -1) {
        value = value.substr(0, lastspace);
      }
    }

    return value + (tail || ' ...');
  };
}

exports.inject = function(module) {
  module.filter('cut', cut);
};
