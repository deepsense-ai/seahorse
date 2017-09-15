'use strict';

exports.inject = function(module) {
  require('./mouse-event.service.js').inject(module);
  require('./page.service.js').inject(module);
  require('./notification.service.js').inject(module);
  require('./time.service.js').inject(module);
};
