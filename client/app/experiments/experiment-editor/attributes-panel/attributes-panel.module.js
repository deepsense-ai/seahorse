/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

exports.inject = function (module) {
  require('./attributes-panel.js').inject(module);
  require('./attributes-list.js').inject(module);

  require('./attribute-types/attribute-boolean/attribute-boolean-type.js').inject(module);
  require('./attribute-types/attribute-single-choice/attribute-single-choice-type.js').inject(module);
  require('./attribute-types/attribute-multiple-choice/attribute-multiple-choice-type.js').inject(module);
  require('./attribute-types/attribute-column-selector/attribute-selector-type.js').inject(module);
  require('./attribute-types/attribute-column-selector/selector-items/type-list-selector-item.js').inject(module);
  require('./attribute-types/attribute-column-selector/selector-items/column-list-selector-item.js').inject(module);
  require('./attribute-types/attribute-column-selector/selector-items/index-list-selector-item.js').inject(module);
  require('./attribute-types/attribute-numeric/attribute-numeric-type.js').inject(module);
  require('./attribute-types/attribute-snippet/attribute-snippet-type.js').inject(module);
  require('./attribute-types/attribute-string/attribute-string-type.js').inject(module);
  require('./attribute-types/attribute-multiplier/attribute-multiplier-type.js').inject(module);
};
