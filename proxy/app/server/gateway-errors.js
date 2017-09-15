/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

var ERRORS = Object.freeze({
  ETIMEDOUT: {
    code: 504,
    title: "Gateway Timeout",
    description: "Connection to service %s timed out, path %s"
  },
  ECONNREFUSED: {
    code: 502,
    title: "Bad Gateway",
    description: "Connection refused to service %s, path %s"
  }
});

var DEFAULT_ERROR = Object.freeze({
  code: 500,
  title: "Error occured",
  description: 'Error while proxying request to service %s, path %s'
});

function getError(code) {
  return ERRORS[code] || DEFAULT_ERROR;
}

module.exports = {
  getError
};
