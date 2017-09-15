'use strict';

import LibraryApiService from './library-api.service.js';


exports.inject = function(module) {
  require('./base-api-client.factory.js').inject(module);
  require('./presets-api.service.js').inject(module);
  require('./workflows-api-client.factory.js').inject(module);
  require('./operations-api-client.factory.js').inject(module);
  require('./operations.factory.js').inject(module);
  require('./operations-hierarchy.service.js').inject(module);
  require('./session-manager-api.service').inject(module);

  module
    .service('LibraryApiService', LibraryApiService);
};
