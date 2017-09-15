(function() {
  'use strict';
  _['sliding'] = function(array, slidingWindow) {
    let iterations = array.length - slidingWindow + 1;
    return _.range(0, iterations).map((i) => array.slice(i, i + slidingWindow))
  };
})();
