'use strict';

exports.inject = function(module) {
  require('./events.service.js').inject(module);
  require('./mouse-event.service.js').inject(module);
  require('./library.service.js').inject(module);
  require('./notification.service.js').inject(module);
  require('./time.service.js').inject(module);
};
