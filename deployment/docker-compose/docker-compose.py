#!/usr/bin/env python
# PYTHON_ARGCOMPLETE_OK
# Copyright 2017 deepsense.ai (CodiLime, Inc)
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


import argparse
import signal
import subprocess
import tempfile
import sys
import os

from docker_compose_generation.generation import *
from docker_compose_generation.configurations import *

sys.path.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), '..', '..', 'utils'))
from api_version import read_api_version


cwd = os.path.join(os.path.dirname(os.path.realpath(__file__)), '..')


git_sha = subprocess.check_output("git rev-parse HEAD", shell=True, cwd=cwd).strip()


def main():
    description = """
    Interface for docker-compose.
    This script's parameters should be followed by docker-compose parameter.
    Example: docker-compose.py up -d
    """
    parser = argparse.ArgumentParser(description=description,
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    parser.add_argument('--operating-system', default=select_os(),
                        help='Operating system that Seahorse will run on. Default selected based on OS.',
                        choices=['linux', 'mac'],
                        action='store')
    parser.add_argument('-b', '--backend-tag', default=git_sha,
                        help='Git tag of the deepsense-backend repo to use',
                        action='store')
    parser.add_argument('-f', '--frontend-tag', default='master-latest',
                        help='Git tag of the deepsense-frontend repo to use',
                        action='store')
    parser.add_argument('--subnet', default='10.255.3.0/24',
                        help='Network address range to use for docker-compose containers',
                        action='store')
    parser.add_argument('--docker-repo', default='',
                        help='Docker repository to use, local by default',
                        action='store')
    parser.add_argument('--custom-frontend', default=None,
                        help='Custom frontend address passed to proxy: HOST:PORT',
                        action='store')
    parser.add_argument('--yaml-file', default='docker-compose.yml',
                        help='The generated file; used only together with --generate-only',
                        action='store')
    parser.add_argument('--generate-only',
                        help='If used, the script will only generate ',
                        action='store_true')
    parser.add_argument('--server-mode',
                        help="If used, Seahorse will listen on 0.0.0.0. This option doesn't work on mac",
                        action='store_true')
    parser.add_argument('--enable-authorization',
                        help='If used, the authorization module will be turned on',
                        action='store_true')

    args, extra_args = parser.parse_known_args()
    try:
        import argcomplete
        argcomplete.autocomplete(parser)
    except ImportError:
        print("Argcomplete is not installed. <tab> autocompletions are not available")
        print("Setup instructions at https://argcomplete.readthedocs.io/en/latest/")

    configuration = {'linux': LinuxConfiguration, 'mac': MacConfiguration}[args.operating_system]

    if args.custom_frontend is not None:
        frontend_address = args.custom_frontend.split(':')
        frontend_address = (frontend_address[0], int(frontend_address[1]))
        configuration.replace(custom_frontend(frontend_address))

    if args.server_mode:
        configuration.replace(ServerModeProxy)

    if args.enable_authorization:
        configuration.replace(EnabledAuthorization)

    docker_compose = dump_yaml_to_string(
        ConfigurationGeneration(configuration).generate(
            GenerationConfig(docker_repository=args.docker_repo,
                             tags={
                                 Repositories.backend: args.backend_tag,
                                 Repositories.frontend: args.frontend_tag
                             },
                             api_version=read_api_version(),
                             subnet=args.subnet)))

    if args.generate_only:
        with open(args.yaml_file, 'w') as f:
            f.write(docker_compose)
    elif not extra_args:
        parser.print_help()
    else:
        with tempfile.NamedTemporaryFile(dir='.', suffix = '.yml') as temp:
            temp.write(docker_compose)
            temp.flush()
            # Ignore interruptions - they'll still be passed to the child process and this
            # process will finish gracefully after the child finishes.
            signal.signal(signal.SIGINT, signal.SIG_IGN)
            subprocess.call(['docker-compose', '-f', temp.name] + extra_args)


def select_os():
    import platform
    if platform.system() == 'Darwin':
        return 'mac'
    elif platform.system() == 'Linux':
        return 'linux'
    else:
        assert False, 'Unknown system: {}'.format(platform.system())

if __name__ == '__main__':
    main()
