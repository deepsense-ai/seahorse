function HelpersService() {
  'use strict';

  this.sliding = function sliding(array, slidingWindow) {
    let iterations = array.length - slidingWindow + 1;
    return _.range(0, iterations).map((i) => array.slice(i, i + slidingWindow));
  };
}

exports.function = HelpersService;

exports.inject = function(module) {
  'use strict';

  module.service('HelpersService', HelpersService);
};
