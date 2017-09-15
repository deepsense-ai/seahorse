'use strict';

export const specialOperations = {
  // Evaluate
  'a88eaf35-9061-4714-b042-ddd2049ce917': {
    icons: ['fa fa-tachometer']
  },
  // Fit
  '0c2ff818-977b-11e5-8994-feff819cdc9f': {
    icons: ['fa fa-gears']
  },
  // Transform
  '643d8706-24db-4674-b5b4-10b5129251fc': {
    icons: ['fa fa-bolt']
  },
  // Fit + Transform
  '1cb153f1-3731-4046-a29b-5ad64fde093f': {
    icons: [
      'fa fa-gears',
      'fa fa-bolt'
    ]
  }
};

exports.inject = function(module) {
  module.constant('specialOperations', specialOperations);
};
