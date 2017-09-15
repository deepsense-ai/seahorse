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

    this.data = {
      subscription: false
    };

    // idle => Nothing happens
    // ws_pending => WebSocket pending connection
    // ws_connected => WebSocket has been connected
    // rb_connected => RabbitMQ has been connected
    // active => Subscription has been done properly
    // reconnecting => In reconnection phase
    // error => Error has been occurred
    this.status = 'idle';
  }

  messageHandler(message) {
    this.status = 'active';

    let parsedBody = JSON.parse(message.body);

    this.$rootScope.$broadcast(
      `ServerCommunication.MESSAGE.${parsedBody.messageType}`,
      parsedBody.messageBody
    );
  }

  onWebSocketConnect() {
    this.status = 'ws_connected';
    this.connectToRabbit();
  }

  onWebSocketConnectError(error) {
    this.status = 'error';
    this.client = this.socket = null;

    this.$log.error(
      'Error has been occurred during WebSocketConnection!',
      error
    );

    if (angular.isString(error) && error.indexOf('Lost connection') > -1) {
      this.reconnect();
    }
  }

  reconnect() {
    this.status = 'reconnecting';
    this.$timeout(() => {
      this.connectToWebSocket();
    }, this.config.socketReconnectionInterval, false);
  }

  subscribeToRabbit() {
    this.status = 'rb_connected';
    this.data.subscription = this.client.subscribe(
      _.template(this.config.queueRoutes.toEditor)({
        workflowId: this.workflowId
      }),
      this.messageHandler.bind(this)
    );
  }

  send(exchangePath = this.config.queueRoutes.seahorseToExecutor, headers = {}, message = {}) {
    return this.client && this.client.send.apply(this.client, arguments);
  }

  updateWorkflow(data) {
    this.send(_.template(this.config.queueRoutes.toExecutor)({
      workflowId: this.workflowId
    }), {}, JSON.stringify({
      messageType: 'updateWorkflow',
      messageBody: data
    }));
  }

  connect() {
    this.send(this.config.queueRoutes.seahorseToExecutor, {}, JSON.stringify({
      messageType: 'connect',
      messageBody: {
        workflowId: this.workflowId
      }
    }));
  }

  connectToRabbit() {
    this.connect();
    this.subscribeToRabbit();
    this.connect();
  }

  launch(workflow, nodesToExecute) {
    this.send(
      _.template(this.config.queueRoutes.toExecutor)({
        workflowId: this.workflowId
      }), {},
      JSON.stringify({
        messageType: 'launch',
        messageBody: {
          workflowId: this.workflowId,
          workflow: workflow.workflow,
          nodesToExecute
        }
      })
    );
  }

  abort() {
    this.send(
      _.template(this.config.queueRoutes.toExecutor)({
        workflowId: this.workflowId
      }), {},
      JSON.stringify({
        messageType: 'abort',
        messageBody: {
          workflowId: this.workflowId
        }
      })
    );
  }

  connectToWebSocket(user = 'guest', pass = 'guest') {
    this.status = 'ws_pending';
    this.socket = new SockJS(`${this.config.socketConnectionHost}stomp`);
    this.client = Stomp.over(this.socket);

    this.client.heartbeat = {
      incoming: 0,
      outgoing: 0
    };

    this.client.connect(
      user,
      pass,
      this.onWebSocketConnect.bind(this, this.workflowId),
      this.onWebSocketConnectError.bind(this)
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
