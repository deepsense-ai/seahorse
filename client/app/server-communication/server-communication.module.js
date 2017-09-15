var angular = require('angular');

var serverCommunication = angular.module('ds.server-communication', []);

require('./server-communication.js').inject(serverCommunication);

module.exports = serverCommunication;
