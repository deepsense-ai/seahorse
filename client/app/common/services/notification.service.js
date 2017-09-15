/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 27.07.15.
 */

'use strict';

class LogHandlingService {
  constructor ($log) {
    this.$log = $log;
  }

  error (message, error) {
    this.$log.error(message, error);
  }
}

class NotificationService extends LogHandlingService {
  constructor ($rootScope, $log, toastr) {
    super($log);

    _.assign(this, {
      'toastr': toastr,
      '$rootScope': $rootScope
    });

    /* Array of all messages in order to delete them after some time */
    this.messages = [];

    this.staticMessages = {
      'Workflow.RUN': {
        message: 'Running workflow ...',
        title: 'Workflow event',

        notificationType: 'info'
      },
      'Workflow.RUN.SUCCESS': {
        message: 'The workflow has been <b>run</b> successfully',
        title: 'Workflow event',

        notificationType: 'success'
      },

      'Workflow.SAVE': {
        message: 'Saving workflow ...',
        title: 'Workflow event',

        notificationType: 'info'
      },
      'Workflow.SAVE.SUCCESS': {
        message: 'The workflow has been <b>saved</b> successfully',
        title: 'Workflow event',

        notificationType: 'success'
      },

      'Workflow.ABORT': {
        message: 'Aborting workflow ...',
        title: 'Workflow event',

        notificationType: 'warning'
      },
      'Workflow.ABORT.SUCCESS': {
        message: 'The workflow has been <b>aborted</b> successfully',
        title: 'Workflow event',

        notificationType: 'success'
      }
    };

    this.dynamicMessages = {
      'Workflow.RUN.ERROR': (event, error) => {
        this.showError(
          NotificationService.getCommonErrorMessage('Workflow RUN', error),
          error
        );
      },
      'Workflow.SAVE.ERROR': (event, error) => {
        this.showError(
          NotificationService.getCommonErrorMessage('Workflow SAVE', error),
          error
        );
      },
      'Workflow.ABORT.ERROR': (event, error) => {
        this.showError(
          NotificationService.getCommonErrorMessage('Workflow ABORT', error),
          error
        );
      }
    };

    this.initEventListeners();
  }

  transportEventToShowByName (event) {
    this.showNotificationByEventName(event.name);
  }

  showError (data, error) {
    this.error(data.title, error);

    let toast = this.toastr.error(data.message, data.title, {
      timeOut: 10000
    });

    this.handleSameMessages(data.message, toast);
    this.replaceInfoMessagesWithSuccess(data.message, toast);
  }

  showNotificationByEventName (eventName) {
    let toast = this.toastr[this.staticMessages[eventName].notificationType](
      this.staticMessages[eventName].message,
      this.staticMessages[eventName].title
    );

    this.handleSameMessages(eventName, toast);
    this.replaceInfoMessagesWithSuccess(eventName, toast);
  }

  handleSameMessages (name, toast) {
    _.remove(this.messages, message => {
      let result = message.name === name;

      if (result) {
        this.toastr.clear(message.toast);
      }

      return result;
    });

    this.messages.push({ name, toast });
  }

  replaceInfoMessagesWithSuccess (name, toast) {
    let regExp = /\.SUCCESS$/;
    let match = name.match(regExp);

    if (match) {
      this.handleSameMessages(name.replace(regExp, ''), toast);
    }
  }

  initEventListeners () {
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

  // Statics
  static getCommonErrorMessage (label, error) {
    return NotificationService.commonMessages(
      'error',
      {
        errorType: label
      },
      error
    );
  }

  static commonMessages (key, templateData, dynamicPart = {}) {
    let data = {
      error: {
        message: `
          <%= errorType %> <b>error</b> <br />
          ${dynamicPart.data} <br />
          ${dynamicPart.statusText}
        `,
        title: 'Workflow event'
      }
    };

    data[key].message = _.template(data[key].message)(templateData);

    return data[key];
  }

  /* @ngInject */
  static factory($rootScope, $log, toastr) {
    NotificationService.instance = new NotificationService($rootScope, $log, toastr);
    return NotificationService.instance;
  }
}

exports.function = NotificationService;

exports.inject = function (module) {
  module.service('NotificationService', NotificationService.factory);
};
