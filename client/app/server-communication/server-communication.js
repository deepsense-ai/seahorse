'use strict';

const EXCHANGE_PATH = '/exchange/some_exchange_name/*.from_editor';

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
    this.$rootScope.$broadcast('ServerCommunication.MESSAGE', message.body);
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

  subscribeToRabbit(
    path = EXCHANGE_PATH,
    messageHandler = this.messageHandler.bind(this)
  ) {
    this.status = 'rb_connected';
    this.subscription = this.client.subscribe(path, messageHandler);
  }

  send(exchangePath = EXCHANGE_PATH, headers = {}, message = {}) {
    return this.client.send.apply(this.client, arguments);
  }

  connectToRabbit() {
    this.status = 'rb_pending';
    // fake: /exchange/named/<user_id>
    // true: /exchange/some_exchange_name/*.from_editor
    this.send(EXCHANGE_PATH, {}, JSON.stringify({
      messageType: 'connect',
      messageBody: {
        workflowId: this.WorkflowService.getWorkflow().id
      }
    }));

    this.subscribeToRabbit();
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
