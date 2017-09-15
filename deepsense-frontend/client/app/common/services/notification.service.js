'use strict';

/**
 * `staticMessages` is map of "Global event name": params
 * is used in order to add dynamic notification messages.
 *
 * Example of usage `staticMessages`:
 *
 * 'Workflow.SAVE': {
 *    message: 'Saving workflow ...',
 *    title: 'Workflow event',
 *    notificationType: 'info'
 * }
 *
 */

class NotificationService {
  constructor($rootScope, $log, toastr) {
    _.assign(this, {$rootScope, $log, toastr});

    /* Array of all messages in order to delete them after some time */
    this.messages = [];
    this.staticMessages = {};

    this.dynamicMessages = {
      'LastExecutionReportService.REPORT_HAS_BEEN_UPLOADED': (event, data) => {
        this.showWithParams({
          message: 'The report for this workflow is already available',
          title: 'Workflow event',
          settings: {
            timeOut: 0,
            extendedTimeOut: 0
          },

          notificationType: 'info'
        });
      }
    };

    this.initEventListeners();
  }

  transportEventToShowByName(event) {
    this.showNotificationByEventName(event.name);
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

  showNotificationByEventName(eventName) {
    let toast = this.toastr[this.staticMessages[eventName].notificationType](
      this.staticMessages[eventName].message,
      this.staticMessages[eventName].title
    );

    this.handleSameMessages(eventName, toast);
    this.replaceInfoMessagesWithSuccess(eventName, toast);
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
    this.messages.map((messsage) => {
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

  initEventListeners() {
    for (let staticEventName in this.staticMessages) {
      if (this.staticMessages.hasOwnProperty(staticEventName)) {
        this.$rootScope.$on(
          staticEventName,
          this.transportEventToShowByName.bind(this)
        );
      }
    }

    for (let dynamicEventName in this.dynamicMessages) {
      if (this.dynamicMessages.hasOwnProperty(dynamicEventName)) {
        this.$rootScope.$on(dynamicEventName, this.dynamicMessages[dynamicEventName]);
      }
    }
  }

}

/* @ngInject */
let createNotificationService = function($rootScope, $log, toastr) {
  NotificationService.instance = new NotificationService($rootScope, $log, toastr);
  return NotificationService.instance;
};
exports.function = NotificationService;

exports.inject = function(module) {
  module.service('NotificationService', createNotificationService);
};
