'use strict';

class LogHandlingService {
  constructor($log) {
    this.$log = $log;
  }

  error(message, error) {
    this.$log.error(message, error);
  }
}

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
 * 'Workflow.SAVE.SUCCESS': {
 *    message: 'The workflow has been <b>saved</b> successfully',
 *    title: 'Workflow event',
 *    notificationType: 'success'
 * }
 */

class NotificationService extends LogHandlingService {
  constructor($rootScope, $log, toastr) {
    super($log);

    _.assign(this, {
      'toastr': toastr,
      '$rootScope': $rootScope
    });

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
      },
      'CopyPaste.COPY.nodes': (event, copyObject) =>
        this.showNodeCopyPaste('copy', copyObject),
      'CopyPaste.PASTE.nodes': (event, pasteObject) =>
        this.showNodeCopyPaste('paste', pasteObject)
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

  showNodeCopyPaste(type, copyObject) {
    let isMultiple = copyObject.entityToCopy.length > 1;
    let node = isMultiple ? copyObject.entityToCopy : copyObject.entityToCopy[0];
    let templateData = {
      entity: 'node',
      multiple: isMultiple,
      eventInThePastText: type === 'copy' ? 'copied' : 'pasted',
      name: isMultiple ?
        `${node.length} nodes` : `
            "${node.uiName || node.name}"
            <span class="o-color-picker o-color-picker--inline"
                  style="background-color: ${node.color}"></span>
          `
    };
    let messageObject = NotificationService.commonMessages(
      type,
      templateData
    );

    messageObject.notificationType = type === 'copy' ? 'info' : 'success';

    this.showWithParams(messageObject);
  }

  showError(data, error) {
    this.error(data.title, error);

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

  // Statics
  static getCommonErrorMessage(label, error) {
    return NotificationService.commonMessages(
      'error', {
        errorType: label
      },
      error
    );
  }

  static commonMessages(key, templateData, dynamicPart = {}) {
    let data = {
      copy: {
        message: `
          ${templateData.multiple? templateData.name : 'The <%= entity %>' + templateData.name + '<br />'}
          ${templateData.multiple? 'are' : 'is'}\
          <strong>${templateData.eventInThePastText}</strong>
        `,
        title: 'Copy/Paste event'
      },
      error: {
        message: `
          <%= errorType %> <b>error</b> <br />
          ${dynamicPart.data} <br />
          ${dynamicPart.statusText}
        `,
        title: 'Workflow event'
      }
    };
    data.paste = data.copy;

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

exports.inject = function(module) {
  module.service('NotificationService', NotificationService.factory);
};
