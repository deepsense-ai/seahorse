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

const _ = require('underscore');

const authorization = {
    "path": "/authorization",
    "host": process.env["AUTHORIZATION_HOST"],
    "name": "sso",
    "proxyTimeout": 1000,
    "timeoutRedirectionPage": "wait.html"
};

const serviceMapping = [authorization, {
    "path": "/v1/workflows",
    "host": process.env["WORKFLOW_MANAGER_HOST"],
    "name": "workflow-manager",
    "proxyTimeout": 100000,
    "auth": "basic"
}, {
    "path": "/v1/presets",
    "host": process.env["WORKFLOW_MANAGER_HOST"],
    "name": "workflow-manager",
    "auth": "basic"
}, {
    "path": "/v1/operations",
    "host": process.env["WORKFLOW_MANAGER_HOST"],
    "name": "workflow-manager",
    "auth": "basic"
}, {
    "path": "/v1/sessions",
    "host": process.env["SESSION_MANAGER_HOST"],
    "name": "session-manager"
}, {
    "path": "/datasourcemanager/v1",
    "host": process.env["DATASOURCE_MANAGER_HOST"],
    "name": "datasource-manager"
}, {
    "path": "/schedulingmanager/v1",
    "host": process.env["SCHEDULING_MANAGER_HOST"],
    "name": "scheduling-manager"
}, {
    "path": "/jupyter",
    "host": process.env["JUPYTER_HOST"],
    "name": "jupyter"
}, {
    "path": "/library",
    "host": process.env["LIBRARY_HOST"],
    "name": "library"
}, {
    "path": "/stomp",
    "host": process.env["RABBITMQ_HOST"],
    "name": "rabbitmq"
}, {
    "path": "/docs",
    "host": process.env["DOCUMENTATION_HOST"],
    "name": "documentation"
}, {
    "path": "/",
    "host": process.env["FRONTEND_HOST"],
    "name": "frontend"
}];

function getServiceForRequest(requestUrl) {
    return _.find(serviceMapping, function (service) {
        return requestUrl.match(service.path);
    });
}

module.exports = {
    getServiceForRequest,
    authorization
};
