"use strict";

function Port(options) {
  var that = this;
  that.init = function init() {
    that.id = options.portId;
    that.index = options.portIndex;
    that.required = options.required;
    that.typeQualifier = options.typeQualifier;
  };
  that.init();
}

module.exports = Port;
