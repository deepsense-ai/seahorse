/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* globals Stomp */

'use strict';

import SockJS from 'sockjs-client';

const _messages = [
  'executionStatus',
  'inferredState',
  'ready',
  'terminated',
  'heartbeat'
];

class ServerCommunication {
  /* @ngInject */
  constructor($log, $q, $timeout, $rootScope, config) {
    _.assign(this, {$log, $q, $timeout, $rootScope, config});

    Stomp.WebSocketClass = SockJS;
    this.connectionAttemptId = Math.floor(Math.random() * 1000000);
    this.exchangeSubscriptions = {};

    this.seahorseTopicListeningUri = () => {
      return `/exchange/seahorse/seahorse.${this.workflowId}.to`;
    };

    this.workflowTopicListeningUri = () => {
      return `/exchange/seahorse/workflow.${this.workflowId}.${this.workflowId}.to`;
    };

    this.workflowTopicSendingUri = () => {
      return `/exchange/seahorse/workflow.${this.workflowId}.${this.workflowId}.from`;
    };
  }

  static isMessageKnown(msgType) {
    return _messages.indexOf(msgType) !== -1;
  }

  messageHandler(uri, message) {
    let parsedBody = JSON.parse(message.body);
    this.$log.info('ServerCommunication messageHandler(uri=' + uri + '): ',
      parsedBody.messageType, parsedBody.messageBody);

    if (!ServerCommunication.isMessageKnown(parsedBody.messageType)) {
      this.$log.error('ServerCommunication messageHandler. Unknown message type "' + parsedBody.messageType + '"');
      return;
    }

    this.$rootScope.$broadcast(
      `ServerCommunication.MESSAGE.${parsedBody.messageType}`,
      parsedBody.messageBody
    );
  }

  errorHandler(connectionAttemptId, error) {
    if (connectionAttemptId !== this.connectionAttemptId) {
      this.$log.info('ServerCommunication onWebSocketConnectError. Ignoring old error.');
      return;
    }
    this.$log.info('ServerCommunication onWebSocketConnectError. Error: ', error);
    this.$log.error('An error has occurred: ', error);
    this.$rootScope.$broadcast('ServerCommunication.CONNECTION_LOST');

    this.client = this.socket = null;
    this.reconnect();
  }

  reconnect() {
    this.$log.info('ServerCommunication reconnect');
    this.$timeout(() => {
      this._connectToWebSocket();
    }, this.config.socketReconnectionInterval, false);
  }

  sendSynchronize() {
    this.send(this.workflowTopicSendingUri(), {}, JSON.stringify({
      messageType: 'synchronize',
      messageBody: {}
    }));
  }

  send(uri, headers = {}, message = {}) {
    this.$log.info('ServerCommunication send, uri ', uri);
    return this.client.send(uri, headers, message);
  }

  sendLaunchToWorkflowExchange(nodesToExecute) {
    this.send(this.workflowTopicSendingUri(), {}, JSON.stringify({
      messageType: 'launch',
      messageBody: {
        workflowId: this.workflowId,
        nodesToExecute: nodesToExecute
      }
    }));
  }

  sendAbortToWorkflowExchange() {
    this.send(this.workflowTopicSendingUri(), {}, JSON.stringify({
      messageType: 'abort',
      messageBody: {
        workflowId: this.workflowId
      }
    }));
  }

  sendUpdateWorkflowToWorkflowExchange(data) {
    this.$log.info('ServerCommunication updateWorkflow');
    this.send(this.workflowTopicSendingUri(), {}, JSON.stringify({
      messageType: 'updateWorkflow',
      messageBody: data
    }));
  }

  // uri - exchange name
  _subscribeToExchange(uri) {
    let previousSubscription = this.exchangeSubscriptions[uri];
    if (previousSubscription) {
      previousSubscription.unsubscribe();
    }

    let newSubscription = this.client.subscribe(uri, this.messageHandler.bind(this, uri));
    this.exchangeSubscriptions[uri] = newSubscription;

    this.$log.info('Subscribe to exchange ' + uri + ', subscription: ', newSubscription);
  }

  unsubscribeFromAllExchanges() {
    for(let key in this.exchangeSubscriptions) {
      if (this.exchangeSubscriptions.hasOwnProperty(key)) {
        this.exchangeSubscriptions[key].unsubscribe();
      }
    }
    this.exchangeSubscriptions = {};
  }

  _onWebSocketConnect() {
    this.$log.info('ServerCommunication onWebSocketConnect');
    this._subscribeToExchange(this.seahorseTopicListeningUri());
    this._subscribeToExchange(this.workflowTopicListeningUri());
    this.$rootScope.$broadcast('ServerCommunication.CONNECTION_ESTABLISHED');
  }

  _connectToWebSocket(user = `${this.config.mqUser}`, pass = `${this.config.mqPass}`) {
    this.socket = new SockJS(`${this.config.socketConnectionHost}stomp`);
    this.client = Stomp.over(this.socket);

    this.client.heartbeat = {
      incoming: 0,
      outgoing: 0
    };

    this.connectionAttemptId = Math.floor(Math.random() * 1000000);

    this.client.connect(
      user,
      pass,
      this._onWebSocketConnect.bind(this),
      this.errorHandler.bind(this, this.connectionAttemptId)
    );
  }

  init(workflowId) {
    this.$log.log('ServerCommunication init', 'Server communication initialized with workflow id ' + workflowId);
    this.workflowId = workflowId; // TODO There should be no state here. Get rid of state in this service.
    this._connectToWebSocket();
  }
}

exports.inject = function(module) {
  module.service('ServerCommunication', ServerCommunication);
};
