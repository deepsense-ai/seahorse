'use strict';

class ServerCommunication {
  /* @ngInject */
  constructor($log, $q, $timeout, $rootScope, config) {
    _.assign(this, {$log, $q, $timeout, $rootScope, config});

    Stomp.WebSocketClass = SockJS;
    this.connectionAttemptId = Math.floor(Math.random() * 1000000);
    this.exchangeSubscriptions = {};

    this.seahorseTopicListeningUri = () => {
      return `/exchange/seahorse/seahorse.${this._getSessionId()}.to`;
    };

    this.workflowTopicListeningUri = () => {
      return `/exchange/seahorse/workflow.${this._getSessionId()}.${this.workflowId}.to`;
    };

    this.workflowTopicSendingUri = () => {
      return `/exchange/seahorse/workflow.${this._getSessionId()}.${this.workflowId}.from`;
    };
  }

  _getSessionId() {
    return this.workflowId;
  }

  static isMessageKnown(msgType) {
    let messages = [
      'executionStatus',
      'inferredState',
      'ready',
      'terminated',
      'workflowWithResults'
    ];
    return messages.indexOf(msgType) !== -1;
  }

  messageHandler(uri, message) {
    let parsedBody = JSON.parse(message.body);
    this.$log.info('ServerCommunication messageHandler(uri=' + uri + '): ',
      parsedBody.messageType, parsedBody.messageBody);

    if (!ServerCommunication.isMessageKnown(parsedBody.messageType)) {
      this.$log.error('ServerCommunication messageHandler. Unknown message type "' + parsedBody.messageType + '"');
      return;
    }

    // When opening workflow two things happen:
    // 1) calling session manager to create executor (we don't know if it already exist or will be created)
    // 2) calling `init` to get workflow data
    //
    // BUT: when there were no executor already in step 1, message in step 2 will most likely not get to the
    // executor, because it didn't have enough time to subscribe to the topic after getting created.
    //
    // That's why we have this code over here. In a case when executor is just created we need to send `init` only
    // after receiving 'ready' message - only that way we are sure it will be reached.
    //
    // We cannot remove step 2 though, because we don't have prior knowledge if executor already existed.
    if (parsedBody.messageType === 'ready') {
      this.sendInitToWorkflowExchange();
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
      this.connectToWebSocket();
    }, this.config.socketReconnectionInterval, false);
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

  sendInitToWorkflowExchange() {
    this.$log.info('ServerCommunication sendInitToWorkflowExchange');
    this.send(this.workflowTopicSendingUri(), {}, JSON.stringify({
      messageType: 'init',
      messageBody: {
        workflowId: this.workflowId
      }
    }));
  }

  // uri - exchange name
  subscribeToExchange(uri) {
    let previousSubscription = this.exchangeSubscriptions[uri];
    if (previousSubscription) {
      previousSubscription.unsubscribe();
    }

    let newSubscription = this.client.subscribe(uri, this.messageHandler.bind(this, uri));
    this.exchangeSubscriptions[uri] = newSubscription;

    this.$log.info('Subscribe to exchange ' + uri + ', subscription: ', newSubscription);
  }

  onWebSocketConnect() {
    this.$log.info('ServerCommunication onWebSocketConnect');
    this.subscribeToExchange(this.seahorseTopicListeningUri());
    this.subscribeToExchange(this.workflowTopicListeningUri());
    this.sendInitToWorkflowExchange();
    this.$rootScope.$broadcast('ServerCommunication.CONNECTION_ESTABLISHED');
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
    console.log('ServerCommunication init', 'Server communication initialized with workflow id ' + workflowId);
    this.workflowId = workflowId;
    this.connectToWebSocket();
  }
}

exports.inject = function(module) {
  module.service('ServerCommunication', ServerCommunication);
};
