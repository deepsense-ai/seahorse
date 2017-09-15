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

const config = {
  // localhost - from docker compose
  'apiHost': window.location.protocol + '//' + window.location.hostname,
  'docsHost': window.location.protocol + '//' + window.location.host,
  'notebookHost': window.location.protocol + '//' + window.location.host + '/jupyter',
  'sessionApiPort': window.location.port,
  'sessionPollingInterval': 1000,
  'apiPort': window.location.port,
  'apiVersion': '1.4.0',
  'editorVersion': '1.4.0',
  'urlApiVersion': 'v1',
  'resultsRefreshInterval': 10000,
  'socketConnectionHost': window.location.protocol + '//' + window.location.host + '/',
  'socketReconnectionInterval': 1000,
  'mqUser': 'yNNp7VJS',
  'mqPass': '1ElYfGNW',
  'libraryPrefix': 'library://'
};

angular.module('ds.lab').constant('config', Object.assign({}, config, window.dockerConfig));
