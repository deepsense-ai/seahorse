/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./base-api-client.factory.js').inject(module);
  require('./workflows-api-client.factory.js').inject(module);
  require('./operations-api-client.factory.js').inject(module);
  require('./entities-api-client.factory.js').inject(module);
  require('./operations.factory.js').inject(module);
  require('./operations-hierarchy.service.js').inject(module);
};
