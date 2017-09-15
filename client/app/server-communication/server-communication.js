'use strict';

class ServerCommunication {
  /* @ngInject */
  constructor($timeout) {

    Stomp.WebSocketClass = SockJS;

    this.socket = new SockJS('http://127.0.0.1:15674/stomp');
    this.client = Stomp.over(this.socket);

    this.client.connect(
      'my_browser', 'my_browser_pass',
      this.onConnect.bind(this), this.onError.bind(this)
    );
  }

  messageHandler(message) {
    console.log(message);
  }

  onConnect() {
    this.subscription =
      this.client.subscribe(
        '/exchange/some_exchange_name/*.from_editor',
        this.messageHandler
      );
  }

  onError() {
    console.log('error');
  }

  send() {
    this.client.send(
      '/exchange/some_exchange_name/*.from_editor', {}, 'Hey hey hey!');
  }
}

exports.inject = function(module) {
  module.service('ServerCommunication', ServerCommunication);
};
