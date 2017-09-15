var Waterline = require('waterline');

module.exports = function(waterline) {
  return Waterline.Collection.extend({
    identity: 'example',
    connection: 'disk',
    attributes: {
      name  : { type: 'string' }
    }
  });
};
