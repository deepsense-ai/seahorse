'use strict';

class ServerCommunication {
  /* @ngInject */
  constructor($log, $q, $timeout, $rootScope, config, WorkflowService) {
    Stomp.WebSocketClass = SockJS;

    this.$log = $log;
    this.$q = $q;
    this.$timeout = $timeout;
    this.$rootScope = $rootScope;
    this.config = config;
    this.WorkflowService = WorkflowService;

    this.data = {
      subscription: false
    };

    // idle => Nothing happens
    // ws_pending => WebSocket pending connection
    // ws_connected => WebSocket has been connected
    // rb_pending => RabbitMQ pending connection
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

    /*(function preventReconnection () {
      setTimeout(() => {
        this.send(this.config.queueRoutes.connect, {}, {});
        preventReconnection();
      }, 15000);
    }.bind(this))();*/
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

  subscribeToRabbit(workflowId) {
    this.status = 'rb_connected';
    this.data.subscription = this.client.subscribe(
      _.template(this.config.queueRoutes.executionStatus)({
        workflowId
      }),
      this.messageHandler.bind(this)
    );
  }

  unSubscribeRabbit(workflowId) {
    this.status = 'rb_pending';
    this.abort(workflowId);
  }

  send(exchangePath = this.config.queueRoutes.connect, headers = {}, message = {}) {
    return this.client && this.client.send.apply(this.client, arguments);
  }

  connectToRabbit() {
    let workflowId = this.WorkflowService.getWorkflow().id;

    this.status = 'rb_pending';
    this.send(this.config.queueRoutes.connect, {}, JSON.stringify({
      messageType: 'connect',
      messageBody: {
        workflowId
      }
    }));

    this.subscribeToRabbit(workflowId);
  }

  launch(workflowId, workflow, nodesToExecute) {
    this.send(
      _.template(this.config.queueRoutes['launch/abort'])({
        workflowId
      }), {},
      JSON.stringify({
        messageType: 'launch',
        messageBody: {
          workflowId, workflow: workflow.workflow, nodesToExecute
        }
      })
    );

    if (this.status === 'rb_pending') {
      this.subscribeToRabbit(workflowId);
    }
  }

  abort(workflowId) {
    this.send(
      _.template(this.config.queueRoutes['launch/abort'])({
        workflowId
      }), {},
      JSON.stringify({
        messageType: 'abort',
        messageBody: {
          workflowId
        }
      })
    );
  }

  connectToWebSocket(user = 'noUser', pass = '123q') {
    this.status = 'ws_pending';
    this.socket = new SockJS(`${this.config.socketConnectionHost}stomp`);
    this.client = Stomp.over(this.socket);

    this.client.connect(
      user,
      pass,
      this.onWebSocketConnect.bind(this),
      this.onWebSocketConnectError.bind(this)
    );
  }

  init() {
    this.connectToWebSocket();
  }
}

exports.inject = function(module) {
  module.service('ServerCommunication', ServerCommunication);
};
