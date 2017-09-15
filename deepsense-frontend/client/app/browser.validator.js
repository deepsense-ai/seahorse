'use strict';

let browser = require('bowser');
let version = browser.version.match(/\d+/)[0];

const SUPPORTED_BROWSERS = {
  Firefox :{
    fullName: 'Mozilla Firefox',
    minVersion: 48
  },
  Chrome :{
    fullName: 'Google Chrome',
    minVersion: 50
  }
};

function isBrowserSupported() {
  return (SUPPORTED_BROWSERS[browser.name] &&
  SUPPORTED_BROWSERS[browser.name].minVersion <= version);
}

function getErrorMessageHTML() {
  return '<div class="alert alert-danger no-support-message" role="alert">' +
    'We\'re sorry, Seahorse doesn\'t support your browser yet.<br/>' +
    'We\'re working on it, in the meantime please use one of the following:' + getSupportedBrowsersText() + '.</div>';
}

function getSupportedBrowsersText() {
  return Object.keys(SUPPORTED_BROWSERS).reduce((text, key, index) => {
    text += (index > 0) ? ' or' : '';
    text += ` ${SUPPORTED_BROWSERS[key].fullName} ver. ${SUPPORTED_BROWSERS[key].minVersion}.0+`;
    return text;
  }, '');
}

module.exports = {
  isBrowserSupported,
  getErrorMessageHTML
};

