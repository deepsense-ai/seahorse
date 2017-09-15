'use strict';

class ServerCommunication {

  /* @ngInject */
  constructor($log, $q, $timeout, $rootScope, config) {
    Stomp.WebSocketClass = SockJS;

    this.$log = $log;
    this.$q = $q;
    this.$timeout = $timeout;
    this.$rootScope = $rootScope;
    this.config = config;
    this.connectionAttemptId = Math.floor(Math.random() * 1000000);

    this.seahorseExchangeListeningUri = () => {
      return '/exchange/' + config.seahorseExchange + '/' + config.topic.toEditor;
    };

    this.seahorseExchangeSendingUri = () => {
      return '/exchange/' + config.seahorseExchange + '/' + config.topic.toExecutor;
    };

    this.workflowExchangeListeningUri = () => {
      return '/exchange/' + this.workflowId + '/' + config.topic.toEditor;
    };

    this.workflowExchangeSendingUri = () => {
      return '/exchange/' + this.workflowId + '/' + config.topic.toExecutor;
    };
  }

  isMessageKnown(msgType) {
    let messages = ['ready', 'workflowWithResults', 'executionStatus', 'inferredState', 'terminated'];
    return messages.indexOf(msgType) !== -1;
  }

  messageHandler(message) {
    let parsedBody = JSON.parse(message.body);
    this.$log.info('ServerCommunication messageHandler messageType and messageBody',
      parsedBody.messageType, parsedBody.messageBody);

    if (!this.isMessageKnown(parsedBody.messageType)) {
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

    this.client = this.socket = null;
    this.reconnect();
  }

  reconnect() {
    this.$log.info('ServerCommunication reconnect');
    this.$timeout(() => {
      this.connectToWebSocket();
    }, this.config.socketReconnectionInterval, false);
  }

  send(uri, headers = {}, message = {}) {
    this.$log.info('ServerCommunication send, uri ', uri);
    return this.client.send(uri, headers, message);
  }

  sendLaunchToWorkflowExchange(nodesToExecute) {
    this.send(this.workflowExchangeSendingUri(), {}, JSON.stringify({
      messageType: 'launch',
      messageBody: {
        workflowId: this.workflowId,
        nodesToExecute: nodesToExecute
      }
    }));
  }

  sendAbortToWorkflowExchange() {
    this.send(this.workflowExchangeSendingUri(), {}, JSON.stringify({
      messageType: 'abort',
      messageBody: {
        workflowId: this.workflowId
      }
    }));
  }

  sendUpdateWorkflowToWorkflowExchange(data) {
    this.$log.info('ServerCommunication updateWorkflow');
    this.send(this.workflowExchangeSendingUri(), {}, JSON.stringify({
      messageType: 'updateWorkflow',
      messageBody: data
    }));
  }

  sendInitToWorkflowExchange() {
    this.$log.info('ServerCommunication sendInitToWorkflowExchange');
    this.send(this.workflowExchangeSendingUri(), {}, JSON.stringify({
      messageType: 'init',
      messageBody: {
        workflowId: this.workflowId
      }
    }));
  }

  sendConnectToSeahorseExchange() {
    this.$log.info('ServerCommunication sendConnectToSeahorseExchange');
    this.send(this.seahorseExchangeSendingUri(), {}, JSON.stringify({
      messageType: 'connect',
      messageBody: {
        workflowId: this.workflowId
      }
    }));
  }

  // uri - exchange name
  subscribeToExchange(uri) {
    var result = this.client.subscribe(uri, this.messageHandler.bind(this));
    this.$log.info('Subscribe to exchange ' + uri + ', result: ', result);
    return result;
  }

  onWebSocketConnect() {
    this.$log.info('ServerCommunication onWebSocketConnect');
    this.subscribeToExchange(this.seahorseExchangeListeningUri());
    this.sendConnectToSeahorseExchange();
    this.$timeout(() => {
      this.subscribeToExchange(this.workflowExchangeListeningUri());
      this.sendInitToWorkflowExchange();
    }, this.config.socketReconnectionInterval, false);
  }

  connectToWebSocket(user = 'guest', pass = 'guest') {
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
      this.onWebSocketConnect.bind(this, this.workflowId),
      this.errorHandler.bind(this, this.connectionAttemptId)
    );
  }

  init(workflowId) {
    this.workflowId = workflowId;
    this.connectToWebSocket();
  }
}

exports.inject = function(module) {
  module.service('ServerCommunication', ServerCommunication);
};
