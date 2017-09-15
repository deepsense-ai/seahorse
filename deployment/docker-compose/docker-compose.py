#!/usr/bin/python

import argparse
import subprocess
import tempfile

from docker_compose_generation.generation import *
from docker_compose_generation.services import *


def read_api_version():
    import os
    import re

    dir_path = os.path.dirname(os.path.realpath(__file__))
    version_sbt = os.path.join(dir_path, '..', '..', 'version.sbt')
    with open(version_sbt) as f:
        content = f.read()
        return re.search('version in ThisBuild := "([0-9.]+).*"', content).group(1)


def main():
    parser = argparse.ArgumentParser(description='Interface for docker-compose.',
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    parser.add_argument('-c', '--configuration', default='linux',
                        help='Configuration to use: linux, mac',
                        action='store')
    parser.add_argument('-d', '--docker-repo', default='docker-repo.deepsense.codilime.com/deepsense_io',
                        help='Docker repository to use',
                        action='store')
    parser.add_argument('-b', '--backend-tag', default='master-latest',
                        help='Git tag of the deepsense-backend repo to use',
                        action='store')
    parser.add_argument('-f', '--frontend-tag', default='master-latest',
                        help='Git tag of the deepsense-frontend repo to use',
                        action='store')
    parser.add_argument('--custom-frontend', default=None,
                        help='Custom frontend address passed to proxy: HOST:PORT',
                        action='store')
    parser.add_argument('-y', '--yaml-file', default='docker-compose.yml',
                        help='The generated file; used only togeter with --generate-only',
                        action='store')
    parser.add_argument('--generate-only',
                        help='If used, the script will only generate ',
                        action='store_true')

    args, extra_args = parser.parse_known_args()

    configuration = {'linux': LinuxConfiguration, 'mac': MacConfiguration}[args.configuration]

    if args.custom_frontend is not None:
        frontend_address = args.custom_frontend.split(':')
        frontend_address = (frontend_address[0], int(frontend_address[1]))
        configuration.direct_to_custom_frontend(frontend_address)

    docker_compose = dump_yaml_to_string(
        ConfigurationGeneration(configuration).generate(
            GenerationConfig(docker_repository=args.docker_repo,
                             tags={
                                 Repositories.backend: args.backend_tag,
                                 Repositories.frontend: args.frontend_tag
                             },
                             api_version=read_api_version())))

    if args.generate_only:
        with open(args.yaml_file, 'w') as f:
            f.write(docker_compose)
    else:
        with tempfile.NamedTemporaryFile(dir='.') as temp:
            temp.write(docker_compose)
            temp.flush()
            subprocess.call(['docker-compose', '-f', temp.name] + extra_args)

if __name__ == '__main__':
    main()
