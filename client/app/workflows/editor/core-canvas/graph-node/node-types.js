'use strict';

export const nodeTypes = {
  PYTHON_NOTEBOOK: 'e76ca616-0322-47a5-b390-70c9668265dd',
  R_NOTEBOOK: '89198bfd-6c86-40de-8238-68f7e0a0b50e',
  CUSTOM_TRANSFORMER: '65240399-2987-41bd-ba7e-2944d60a3404',
  CUSTOM_TRANSFORMER_SOURCE: 'f94b04d7-ec34-42f7-8100-93fe235c89f8',
  CUSTOM_TRANSFORMER_SINK: 'e652238f-7415-4da6-95c6-ee33808561b2'
};

exports.inject = function(module) {
  module.constant('nodeTypes', nodeTypes);
};
