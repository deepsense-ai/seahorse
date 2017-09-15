'use strict';

class NotificationService {
  constructor($rootScope, $log, toastr) {
    _.assign(this, {$rootScope, $log, toastr});

    /* Array of all messages in order to delete them after some time */
    this.messages = [];
  }

  showWithParams(params) {
    let toast = this.toastr[params.notificationType](params.message, params.title, params.settings);

    this.handleSameMessages(params.message, toast);
    this.replaceInfoMessagesWithSuccess(params.message, toast);
  }

  showError(data, error) {
    this.$log.error(data.title, error);

    let toast = this.toastr.error(data.message, data.title, {
      timeOut: 10000
    });

    this.handleSameMessages(data.message, toast);
    this.replaceInfoMessagesWithSuccess(data.message, toast);
  }

  handleSameMessages(name, toast) {
    _.remove(this.messages, message => {
      let result = message.name === name;

      if (result) {
        this.toastr.clear(message.toast);
      }

      return result;
    });

    this.messages.push({
      name, toast
    });
  }

  clearToasts() {
    this.messages.forEach((messsage) => {
      this.toastr.clear(messsage.toast);
    });
  }

  replaceInfoMessagesWithSuccess(name, toast) {
    let regExp = /\.SUCCESS$/;
    let match = name.match(regExp);

    if (match) {
      this.handleSameMessages(name.replace(regExp, ''), toast);
    }
  }

}

/* @ngInject */
let createNotificationService = function ($rootScope, $log, toastr) {
  NotificationService.instance = new NotificationService($rootScope, $log, toastr);
  return NotificationService.instance;
};
exports.function = NotificationService;

exports.inject = function(module) {
  module.service('NotificationService', createNotificationService);
};
