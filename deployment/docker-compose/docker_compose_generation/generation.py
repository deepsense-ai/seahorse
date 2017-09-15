import yaml

from docker_compose_utils import *


class GenerationConfig(object):
    def __init__(self, docker_repository, tags, api_version):
        self.docker_repository = docker_repository
        self.tags = tags
        self.api_version = api_version

    def tag(self, repository):
        return self.tags[repository]


class VolumesGeneration(object):
    @staticmethod
    def generate(volumes):
        return dict((v, {}) for v in volumes)


class ServiceGeneration(object):

    def __init__(self, service):
        self.service = service

    def generate(self, generation_config):
        properties = {
            'image': '{}/deepsense-{}:{}'.format(
                generation_config.docker_repository,
                self.service.image_name(),
                generation_config.tag(self.service.repository())),
            'network_mode': self.service.network_mode or None,
            'environment': self.service.environment().to_dict() or None,
            'depends_on': [c.name() for c in self.service.depends_on()] or None,
            'links': [c.name() for c in self.service.links()] or None,
            'volumes': self.service.volumes() or None,
            'ports': [Ports.exposed_on_localhost(pm.exposed, pm.internal) for pm in self.service.port_mapping()],
            'restart': self.service.restart
        }

        return self.service.name(), {k: v for (k, v) in properties.iteritems() if v is not None}


class ConfigurationGeneration(object):

    def __init__(self, configuration):
        self.configuration = configuration
        self.dependencies = Dependencies()
        self.service_instances = [s(self.dependencies) for s in self.configuration.services]
        for si in self.service_instances:
            self.dependencies.add_service(si)

    def generate(self, generation_config):
        self.dependencies.Frontend.API_VERSION = generation_config.api_version

        return {
            'version': '2',
            'services': dict(ServiceGeneration(s).generate(generation_config) for s in self.service_instances),
            'volumes': VolumesGeneration.generate(self.configuration.volumes)
        }


def dump_yaml_to_string(json_obj):
    class MyDumper(yaml.Dumper):
        def increase_indent(self, flow=False, indentless=False):
            return super(MyDumper, self).increase_indent(flow, False)

    return yaml.dump(json_obj, Dumper=MyDumper, default_flow_style=False)
