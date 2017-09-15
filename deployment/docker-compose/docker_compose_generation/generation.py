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


import yaml

from docker_compose_utils import *


class GenerationConfig(object):
    def __init__(self, docker_repository, tags, api_version, subnet):
        self.docker_repository = docker_repository
        self.tags = tags
        self.api_version = api_version
        self.subnet = Subnet(subnet)
        self.gateway = self.subnet.default_gateway()

    def tag(self, repository):
        return self.tags[repository]


class VolumesGeneration(object):
    @staticmethod
    def generate(volumes):
        return dict((v, {}) for v in volumes)


class NetworksGeneration(object):
    @staticmethod
    def generate(generation_config):
        return {
            'default': {
                'ipam': {
                    'driver': 'default',
                    'config': [
                        {
                            'subnet': generation_config.subnet.as_string(),
                            'gateway': generation_config.gateway
                        }
                    ]
                }
            }
        }


class ServiceGeneration(object):

    def __init__(self, service):
        self.service = service

    def generate(self, generation_config):
        if generation_config.docker_repository != '':
            repository_prefix = "{}/".format(generation_config.docker_repository)
        else:
            repository_prefix = ''
        properties = {
            'image': '{}seahorse-{}:{}'.format(
                repository_prefix,
                self.service.image_name(),
                generation_config.tag(self.service.repository())),
            'network_mode': self.service.network_mode or None,
            'environment': self.service.environment().to_dict() or None,
            'depends_on': [c.name() for c in self.service.depends_on()] or None,
            'links': [c.name() for c in self.service.links()] or None,
            'volumes': self.service.volumes() or None,
            'ports': [Ports.exposed_on_localhost(pm.exposed, pm.internal)
                      for pm in self.service.port_mapping()
                      if self.service.port_mapping().generate],
            'restart': self.service.restart
        }

        if self.service.network_mode != 'host':
            properties['networks'] = {
                'default': {
                    'ipv4_address': self.service.internal_ip().host
                }
            }

        return self.service.name(), {k: v for (k, v) in properties.iteritems() if v is not None}


class ConfigurationGeneration(object):

    def __init__(self, configuration):
        self.configuration = configuration
        self.services = Services()


    def generate(self, generation_config):
        # Each service receives a full list of service instances so that it may use
        # their properties (like addresses, port numbers, etc)
        self.service_instances = [s(self.services, generation_config) for s in self.configuration.services]
        for si in self.service_instances:
            self.services.add_service(si)

        self.services.Frontend.API_VERSION = generation_config.api_version

        return {
            'version': '2',
            'services': dict(ServiceGeneration(s).generate(generation_config) for s in self.service_instances),
            'volumes': VolumesGeneration.generate(self.configuration.volumes),
            'networks': NetworksGeneration.generate(generation_config)
        }


def dump_yaml_to_string(json_obj):
    class MyDumper(yaml.Dumper):
        def increase_indent(self, flow=False, indentless=False):
            return super(MyDumper, self).increase_indent(flow, False)

    return yaml.dump(json_obj, Dumper=MyDumper, default_flow_style=False)
