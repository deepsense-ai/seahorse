# Copyright 2016 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


import json

from docker_compose_generation.docker_compose_utils import *


class Directories(object):

    library = './library'
    data = './data'
    h2_data = './h2-data'
    jars = './jars'
    r_libs = './R_Libs'
    spark_application_logs = './spark_applications_logs'

    @staticmethod
    def expose(external, internal, mount=None):
        if mount is None:
            return '{}:{}'.format(external, internal)
        else:
            return '{}:{}:{}'.format(external, internal, mount)


class Service(object):

    restart = 'always'

    network_mode = None

    enable_authorization = 'false'

    def __init__(self, services, generation_config):
        self.services = services
        self.generation_config = generation_config
        self.service_no = -1

    def exposed_address(self, name=None):
        return Address('127.0.0.1', self.port_mapping().get(name).exposed)

    def internal_address(self, name=None):
        return Address(self.name(), self.port_mapping().get(name).internal)

    def internal_ip(self, name=None):
        address = self.internal_address(name)
        address.host = self.generation_config.subnet.subnet_ip(self.service_no + 2)
        return address

    def port_mapping(self):
        return PortMappings()

    def volumes(self):
        return []

    @classmethod
    def image_name(cls):
        return cls.__name__.lower()

    @classmethod
    def name(cls):
        return cls.__name__.lower()

    def repository(self):
        return Repositories.backend

    def depends_on(self):
        return []

    def environment(self):
        return Env()

    def links(self):
        if self.network_mode == 'host':
            return []
        return [d for d in self.depends_on() if d.network_mode != 'host']


class Documentation(Service):
    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(80, 60112))


class Mail(Service):
    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(25, 60111))


class Proxy(Service):

    def depends_on(self):
        return [
            WorkflowManager,
            SessionManager,
            DataSourceManager,
            SchedulingManager,
            Library,
            Notebooks,
            RabbitMQ,
            Frontend,
            Authorization,
            Documentation
        ]

    def environment(self):
        return Env(
            ENABLE_AUTHORIZATION=self.services.Authorization.enable_authorization(),
            FORCE_HTTPS='false',
            WORKFLOW_MANAGER_HOST=self._service_address(WorkflowManager),
            SCHEDULING_MANAGER_HOST=self._service_address(SchedulingManager),
            DATASOURCE_MANAGER_HOST=self._service_address(DataSourceManager),
            SESSION_MANAGER_HOST=self._service_address(SessionManager),
            LIBRARY_HOST=self._service_address(Library),
            JUPYTER_HOST=self._service_address(Notebooks),
            FRONTEND_HOST=self._service_address(Frontend),
            DOCUMENTATION_HOST=self._service_address(Documentation),
            AUTHORIZATION_HOST=self._service_address(Authorization),
            RABBITMQ_HOST=self._service_address(RabbitMQ, 'websocket'),
            PORT=33321) + \
               self.services.WorkflowManager.credentials().as_env()


    def port_mapping(self):
        mappings = PortMappings().add(PortMappings.Mapping(33321, 33321))
        mappings.generate = True
        return mappings


    def _service_address(self, service, name=None):
        address_no_protocol = getattr(self.services, service.name()).internal_address(name).as_string()
        return "http://{}".format(address_no_protocol)


class SchedulingManager(Service):

    def depends_on(self):
        return [
            Database,
            SessionManager,
            WorkflowManager,
            Mail,
        ]

    def environment(self):
        return super(SchedulingManager, self).environment() + \
               Env(
                   PORT=self.port_mapping().get().internal,
                   SEAHORSE_EXTERNAL_URL="http://localhost:33321/",
                   JDBC_URL=self.services.Database.internal_jdbc_url(db='schedulingmanager'),
                   SM_URL='http://{}'.format(self.services.SessionManager.internal_address()),
                   WM_URL='http://{}'.format(self.services.WorkflowManager.internal_address())) + \
               self.services.Mail.internal_address().as_env('MAIL_SERVER_HOST', 'MAIL_SERVER_PORT') + \
               self.services.WorkflowManager.credentials().as_env()

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(60110, 60110))


class SessionManager(Service):

    network_mode = 'host'

    def depends_on(self):
        return [
            RabbitMQ,
            WorkflowManager,
            Library,
            Database,
        ]

    def environment(self):
        return Env(
            SM_HOST=self.generation_config.gateway,
            SM_PORT=self.port_mapping().get().internal,
            JDBC_URL=self.services.Database.internal_ip_jdbc_url(db='sessionmanager'),
            NOTEBOOK_SERVER_ADDRESS='http://{}'.format(self.services.Notebooks.internal_ip().as_string()),
            DATASOURCE_SERVER_ADDRESS=self.services.DatasourceManager.internal_ip_datasource_url(),
            SX_PARAM_SESSION_EXECUTOR_PATH='/opt/docker/we.jar',
            SX_PARAM_SESSION_EXECUTOR_DEPS_PATH='/opt/docker/we-deps.zip',
            SX_PARAM_PYTHON_EXECUTOR_BINARY='python',
            SX_PARAM_SPARK_RESOURCES_JARS='/resources/jars',
            SX_PARAM_SPARK_APPLICATIONS_LOGS_DIR='/spark_applications_logs',
            SX_PARAM_TEMP_DIR='/tmp/seahorse/download',
            SX_PARAM_PYTHON_DRIVER_BINARY='/opt/conda/bin/python',
            SX_PARAM_WM_ADDRESS=self.services.WorkflowManager.internal_ip().as_string()) + \
               self.services.RabbitMQ.credentials().as_env() + \
               self.services.RabbitMQ.internal_ip().as_env('MQ_HOST', 'MQ_PORT') + \
               self.services.WorkflowManager.credentials().as_env('SX_PARAM_WM_AUTH_USER', 'SX_PARAM_WM_AUTH_PASS') + \
               self.services.Mail.internal_ip().as_env('MAIL_SERVER_HOST', 'MAIL_SERVER_PORT')

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(9082, 60100))

    def volumes(self):
        return [
            Directories.expose(Directories.data, '/resources/data'),
            Directories.expose(Directories.jars, '/resources/jars'),
            Directories.expose(Directories.r_libs, '/opt/R_Libs'),
            Directories.expose(Directories.spark_application_logs, '/spark_applications_logs', 'rw'),
            Directories.expose(Directories.library, '/library')
        ]

    def internal_address(self, name=None):
        return Address(self.generation_config.gateway, self.port_mapping().get(name).internal)


class SessionManagerBridgeNetwork(SessionManager):

    network_mode = None

    @classmethod
    def name(cls):
        return 'sessionmanager'

    @classmethod
    def image_name(cls):
        return 'sessionmanager'

    def environment(self):
        return super(SessionManagerBridgeNetwork, self).environment() + \
               Env(
                   SM_HOST='0.0.0.0',
                   SM_PORT=self.port_mapping().get().internal,
                   JDBC_URL=self.services.Database.internal_jdbc_url(db='sessionmanager'),
                   NOTEBOOK_SERVER_ADDRESS='http://{}'.format(self.services.Notebooks.internal_address().as_string()),
                   DATASOURCE_SERVER_ADDRESS=self.services.DatasourceManager.internal_datasource_url(),
                   SX_PARAM_WM_ADDRESS=self.services.WorkflowManager.internal_address().as_string()) + \
               self.services.RabbitMQ.internal_address().as_env('MQ_HOST', 'MQ_PORT') + \
               self.services.Mail.internal_address().as_env('MAIL_SERVER_HOST', 'MAIL_SERVER_PORT')

    def internal_address(self, name=None):
        return Address(self.name(), self.port_mapping().get(name).internal)



class WorkflowManager(Service):

    def depends_on(self):
        return [
            Database,
            DataSourceManager
        ]

    def environment(self):
        return Env(
            WM_HOST='0.0.0.0',
            WM_PORT=self.port_mapping().get().exposed,
            JDBC_URL=self.services.Database.internal_jdbc_url(db='workflowmanager'),
            DATASOURCE_SERVER_ADDRESS=self.services.DatasourceManager.internal_datasource_url()) + \
               self.credentials().as_env()

    @staticmethod
    def credentials():
        return Credentials('oJkTZ8BV', '8Ep9GqRr', 'WM_AUTH_USER', 'WM_AUTH_PASS')

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(60103, 60103))

    def volumes(self):
        return [
            Directories.expose(Directories.jars, '/resources/jars')
        ]


class Frontend(Service):
    API_VERSION = None  # This will be set during generation, in runtime

    def repository(self):
        return Repositories.frontend

    def depends_on(self):
        return [
            Documentation,
            WorkflowManager,
            SessionManager,
            Library,
            Notebooks,
            RabbitMQ
        ]

    def environment(self):
        assert self.API_VERSION is not None
        return Env(
            SESSION_POLLING_INTERVAL=1000,
            PORT=80,
            API_VERSION=self.API_VERSION) +\
               self.services.RabbitMQ.credentials().as_env()

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(80, 60106))


class RabbitMQ(Service):

    def environment(self):
        return self.credentials().as_env('RABBITMQ_USER', 'RABBITMQ_PASS')

    def port_mapping(self):
        return PortMappings() \
            .add(PortMappings.Mapping(5672, 60101)) \
            .add(PortMappings.Mapping(15674, 60102), name='websocket')

    @staticmethod
    def credentials():
        return Credentials('yNNp7VJS', '1ElYfGNW', 'MQ_USER', 'MQ_PASS')


class Database(Service):
    def image_name(self):
        return 'h2'

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(1521, 60104))

    def volumes(self):
        return [
            Directories.expose(Directories.h2_data, '/opt/h2-data', 'rw')
        ]

    def internal_jdbc_url(self, db):
        return 'jdbc:h2:tcp://{}/{};DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1'.format(self.internal_address(), db)

    def internal_ip_jdbc_url(self, db):
        return 'jdbc:h2:tcp://{}/{};DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1'.format(self.internal_ip(), db)

    def exposed_jdbc_url(self, db):
        return 'jdbc:h2:tcp://{}/{};DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1'.format(self.exposed_address(), db)


class Notebooks(Service):
    def depends_on(self):
        return [
            RabbitMQ,
            WorkflowManager
        ]

    def environment(self):
        return Env(
            MISSED_HEARTBEAT_LIMIT=30,
            WM_URL='http://{}'.format(self.services.WorkflowManager.internal_address().as_string()),
            JUPYTER_LISTENING_IP='0.0.0.0',
            JUPYTER_LISTENING_PORT=self.port_mapping().get().internal,
            HEARTBEAT_INTERVAL=2.0) \
               + self.services.WorkflowManager.credentials().as_env() \
               + self.services.RabbitMQ.credentials().as_env() \
               + self.services.RabbitMQ.internal_address().as_env('MQ_HOST', 'MQ_PORT')

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(8888, 60105))


class Authorization(Service):

    def depends_on(self):
        return [
            Database
        ]

    def environment(self):
        return Env(
            ENABLE_AUTHORIZATION=self.enable_authorization(),
            JDBC_URL=self.services.Database.internal_jdbc_url(db="uaa"),
            SEAHORSE_ADMIN_EMAIL="seahorse-admin@deepsense.ai")

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(8080, 60109))

    @staticmethod
    def enable_authorization():
        return 'false'


class DataSourceManager(Service):

    def depends_on(self):
        return [
            Database
        ]

    def environment(self):
        return Env(
            JDBC_URL=self.services.Database.internal_jdbc_url(db='datasourcemanager'))

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(8080, 60108))

    def internal_datasource_url(self):
        return 'http://{}/datasourcemanager/v1/'.format(self.internal_address())

    def exposed_datasource_url(self):
        return 'http://{}/datasourcemanager/v1/'.format(self.exposed_address())

    def internal_ip_datasource_url(self):
       return 'http://{}/datasourcemanager/v1/'.format(self.internal_ip())


class Library(Service):

    @classmethod
    def image_name(cls):
        return 'libraryservice'

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(9083, 60107))

    def volumes(self):
        return [
            Directories.expose(Directories.library, '/library')
        ]


def custom_frontend(frontend_address):
    class CustomFrontend(Frontend):
        @classmethod
        def name(cls):
            return 'frontend'

        @classmethod
        def image_name(cls):
            return 'frontend'

        # noinspection PyUnusedLocal,PyMethodMayBeStatic
        def get_address(self, name=None):
            return Address(frontend_address[0], frontend_address[1])

        exposed_address = get_address
        internal_address = get_address

    return CustomFrontend


class ServerModeProxy(Proxy):
    @classmethod
    def name(cls):
        return 'proxy'

    @classmethod
    def image_name(cls):
        return 'proxy'

    def environment(self):
        return super(ServerModeProxy, self).environment() + \
            Env(HOST='0.0.0.0')


class EnabledAuthorization(Authorization):
    @classmethod
    def name(cls):
        return 'authorization'

    @classmethod
    def image_name(cls):
        return 'authorization'

    @staticmethod
    def enable_authorization():
        return 'true'


class Configuration(object):
    services = []
    volumes = []

    @classmethod
    def replace(cls, service):
        cls.services = [service] + [s for s in cls.services if s.name().lower() != service.name().lower()]


class LinuxConfiguration(Configuration):

    services = [
        SessionManager,
        SchedulingManager,
        Mail,
        Documentation,
        Proxy,
        Frontend,
        Library,
        RabbitMQ,
        Authorization,
        Notebooks,
        WorkflowManager,
        Database,
        DataSourceManager
    ]



class MacConfiguration(Configuration):

    services = [
        SessionManagerBridgeNetwork,
        SchedulingManager,
        Mail,
        Documentation,
        Proxy,
        Frontend,
        Library,
        RabbitMQ,
        Authorization,
        Notebooks,
        WorkflowManager,
        Database,
        DataSourceManager
    ]
