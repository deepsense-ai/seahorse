# Copyright (c) 2016, CodiLime Inc.

import json

from docker_compose_generation.docker_compose_utils import *


class Directories(object):
    class Volumes(object):
        library = 'library'

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

    def __init__(self, deps):
        self.deps = deps

    def exposed_address(self, name=None):
        return Address('127.0.0.1', self.port_mapping().get(name).exposed)

    def internal_address(self, name=None):
        return Address(self.name(), self.port_mapping().get(name).internal)

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


class Mail(Service):
    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(25, 60111))

class Proxy(Service):

    network_mode = 'host'

    def depends_on(self):
        return [
            WorkflowManager,
            SessionManager,
            SchedulingManager,
            Library,
            Notebooks,
            RabbitMQ,
            Frontend
        ]

    def environment(self):
        return Env(
            VCAP_SERVICES=json.dumps(self.vcap_services()),
            HOST='127.0.0.1',
            ENABLE_AUTHORIZATION=self.enable_authorization,
            FORCE_HTTPS='false',
            PORT=33321) + \
               self.deps.WorkflowManager.credentials().as_env()

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(33321, 33321))

    def service_address(self, service, name=None):
        return getattr(self.deps, service.name()).exposed_address(name).as_string()

    def vcap_services(self):
        def service_desc(service_name, service):
            return {
                "credentials": {
                    "host": "http://{}".format(self.service_address(service))
                },
                "name": service_name
            }

        return {
            "user-provided": [
                service_desc('workflow-manager', WorkflowManager),
                service_desc('scheduling-manager', SchedulingManager),
                service_desc('library', Library),
                service_desc('session-manager', SessionManager),
                service_desc('jupyter', Notebooks),
                service_desc('frontend', Frontend),
                {
                    "credentials": {
                        "host": "http://{}".format(self.service_address(Authorization)),
                        "authorizationUri": "/authorization/oauth/authorize",
                        "logoutUri": "/authorization/logout.do",
                        "tokenUri": "http://{}/authorization/oauth/token".format(self.service_address(Authorization)),
                        "clientId": "Seahorse",
                        "clientSecret": "seahorse01",
                        "userInfoUri": "http://{}/authorization/userinfo".format(self.service_address(Authorization))
                    },
                    "name": "sso"
                },
                {
                    "credentials": {
                        "host": "http://{}".format(self.service_address(RabbitMQ, 'websocket'))
                    },
                    "name": "rabbitmq"
                }
            ]
        }

class SchedulingManagerBase(Service):
    def exposed_port(self):
        return 60110

    def depends_on(self):
        return [
            Database,
            SessionManager,
            WorkflowManager,
            Mail,
        ]

    def environment(self):
        return (Env(
            PORT=self.exposed_port(),
            SEAHORSE_EXTERNAL_URL="http://localhost:33321/") +
                self.deps.WorkflowManager.credentials().as_env())

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(self.exposed_port(), self.exposed_port()))

class SchedulingManager(SchedulingManagerBase):

    network_mode = 'host'

    def environment(self):
        return super(SchedulingManager, self).environment() + \
               Env(
                   JDBC_URL=self.deps.Database.exposed_jdbc_url(db='schedulingmanager'),
                   SM_URL='http://{}'.format(self.deps.SessionManager.exposed_address()),
                   WM_URL='http://{}'.format(self.deps.WorkflowManager.exposed_address()),
               )

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
            SM_HOST='127.0.0.1',
            SM_PORT=self.port_mapping().get().exposed,
            JDBC_URL=self.deps.Database.exposed_jdbc_url(db='sessionmanager'),
            SX_PARAM_SESSION_EXECUTOR_PATH='/opt/docker/we.jar',
            SX_PARAM_SESSION_EXECUTOR_DEPS_PATH='/opt/docker/we-deps.zip',
            SX_PARAM_PYTHON_EXECUTOR_BINARY='python',
            SX_PARAM_SPARK_RESOURCES_JARS='/resources/jars',
            SX_PARAM_SPARK_APPLICATIONS_LOGS_DIR='/spark_applications_logs',
            SX_PARAM_TEMP_DIR='/tmp/seahorse/download',
            SX_PARAM_PYTHON_DRIVER_BINARY='/opt/conda/bin/python',
            SX_PARAM_WM_ADDRESS=self.deps.WorkflowManager.exposed_address().as_string()) + \
               self.deps.RabbitMQ.credentials().as_env() + \
               self.deps.RabbitMQ.exposed_address().as_env('MQ_HOST', 'MQ_PORT') + \
               self.deps.WorkflowManager.credentials().as_env('SX_PARAM_WM_AUTH_USER', 'SX_PARAM_WM_AUTH_PASS')

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(9082, 60100))

    def volumes(self):
        return [
            Directories.expose(Directories.data, '/resources/data'),
            Directories.expose(Directories.jars, '/resources/jars'),
            Directories.expose(Directories.r_libs, '/opt/R_Libs'),
            Directories.expose(Directories.spark_application_logs, '/spark_applications_logs', 'rw'),
            Directories.expose(Directories.Volumes.library, '/library')
        ]


class WorkflowManager(Service):

    def depends_on(self):
        return [
            Database
        ]

    def environment(self):
        return Env(
            JDBC_URL=self.deps.Database.internal_jdbc_url(db='workflowmanager')) + \
               self.credentials().as_env()

    @staticmethod
    def credentials():
        return Credentials('oJkTZ8BV', '8Ep9GqRr', 'WM_AUTH_USER', 'WM_AUTH_PASS')

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(9080, 60103))

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
               self.deps.RabbitMQ.credentials().as_env()

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
            WM_URL='http://{}'.format(self.deps.WorkflowManager.internal_address()),
            HEARTBEAT_INTERVAL=2.0) \
               + self.deps.RabbitMQ.credentials().as_env() \
               + self.deps.RabbitMQ.internal_address().as_env('MQ_HOST', 'MQ_PORT') \
               + self.deps.WorkflowManager.credentials().as_env()

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(8888, 60105))


class Authorization(Service):

    def depends_on(self):
        return [
            Database
        ]

    def environment(self):
        return Env(
            ENABLE_AUTHORIZATION=self.enable_authorization)

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(8080, 60109))


class DataSourceManager(Service):

    def depends_on(self):
        return [
            Database
        ]

    def environment(self):
        return Env(
            JDBC_URL=self.deps.Database.internal_jdbc_url(db='datasourcemanager'))

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(8080, 60108))


class Library(Service):

    @classmethod
    def image_name(cls):
        return 'libraryservice'

    def port_mapping(self):
        return PortMappings().add(PortMappings.Mapping(9083, 60107))

    def volumes(self):
        return [
            Directories.expose(Directories.Volumes.library, '/library')
        ]


class ProxyBridgeNetwork(Proxy):

    network_mode = None

    @classmethod
    def name(cls):
        return 'proxy'

    @classmethod
    def image_name(cls):
        return 'proxy'

    def environment(self):
        return super(ProxyBridgeNetwork, self).environment() + \
               Env(
                   HOST='0.0.0.0')

    def service_address(self, service, name=None):
        return getattr(self.deps, service.name()).internal_address(name).as_string()


class SchedulingManagerBridgeNetwork(SchedulingManagerBase):
    network_mode = None

    @classmethod
    def name(cls):
        return 'schedulingmanager'

    @classmethod
    def image_name(cls):
        return 'schedulingmanager'

    def environment(self):
        return super(SchedulingManagerBridgeNetwork, self).environment() + \
               Env(
                   HOST='0.0.0.0',
                   JDBC_URL=self.deps.Database.internal_jdbc_url(db='schedulingmanager'),
                   SM_URL='http://{}'.format(self.deps.SessionManager.internal_address()),
                   WM_URL='http://{}'.format(self.deps.WorkflowManager.internal_address()),
               )

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
                   JDBC_URL=self.deps.Database.internal_jdbc_url(db='sessionmanager'),
                   SX_PARAM_WM_ADDRESS=self.deps.WorkflowManager.internal_address().as_string()) + \
               self.deps.RabbitMQ.internal_address().as_env('MQ_HOST', 'MQ_PORT')

class Configuration(object):
    services = []
    volumes = []

    @classmethod
    def direct_to_custom_frontend(cls, frontend_address):
        class CustomFrontend(Frontend):
            @classmethod
            def name(cls):
                return 'frontend'

            @classmethod
            def image_name(cls):
                return 'sessionmanager'

            # noinspection PyUnusedLocal,PyMethodMayBeStatic
            def get_address(self, name=None):
                return Address(frontend_address[0], frontend_address[1])

            exposed_address = get_address
            internal_address = get_address

        cls.services = [s for s in cls.services if s.name().lower() != 'frontend'] + [CustomFrontend]


class LinuxConfiguration(Configuration):

    services = [
        SessionManager,
        SchedulingManager,
        Mail,
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

    volumes = [
        Directories.Volumes.library
    ]


class MacConfiguration(Configuration):

    services = [
        SessionManagerBridgeNetwork,
        SchedulingManagerBridgeNetwork,
        Mail,
        ProxyBridgeNetwork,
        Frontend,
        Library,
        RabbitMQ,
        Authorization,
        Notebooks,
        WorkflowManager,
        Database,
        DataSourceManager
    ]

    volumes = [
        Directories.Volumes.library
    ]
