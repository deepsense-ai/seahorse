/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
