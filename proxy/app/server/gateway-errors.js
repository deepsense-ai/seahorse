/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var ERRORS = Object.freeze({
  ETIMEDOUT: {
    code: 504,
    title: "Gateway Timeout",
    description: "Connection to service %s timed out, path %s"
  },
  ECONNREFUSED: {
    code: 502,
    title: "Bad Gateway",
    description: "Connection refused to service %s, path %s"
  }
});

var DEFAULT_ERROR = Object.freeze({
  code: 500,
  title: "Error occured",
  description: 'Error while proxying request to service %s, path %s'
});

function getError(code) {
  return ERRORS[code] || DEFAULT_ERROR;
}

module.exports = {
  getError
};
