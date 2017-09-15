'use strict';

function precision() {
  const DEFAULT_PRECISION = 4;

  return function(value, digits) {
    const numVal = parseFloat(value);

    if (_.isNaN(numVal)) {
      return value;
    }

    if (_.isUndefined(digits)) {
      digits = DEFAULT_PRECISION;
    }

    return numVal.toPrecision(digits);
  };
}

exports.inject = function(module) {
  module.filter('precision', precision);
};
