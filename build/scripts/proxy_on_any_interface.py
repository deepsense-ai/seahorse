#!/usr/bin/env python
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

import yaml
import sys


def load_compose(filename):
    with open(filename) as f:
        return yaml.load(f)


# taken from docker-compose.py
def dump_yaml(json_obj, stream=None):
    class MyDumper(yaml.Dumper):
        def increase_indent(self, flow=False, indentless=False):
            return super(MyDumper, self).increase_indent(flow, False)

    return yaml.dump(json_obj,
                     stream=stream,
                     Dumper=MyDumper,
                     default_flow_style=False)


def local_to_any_in_port_mapping(mapping):
    return mapping.replace('127.0.0.1', '0.0.0.0')


def update_proxy_port_mapping(yaml):
    yaml['services']['proxy']['ports'] = \
        map(local_to_any_in_port_mapping, yaml['services']['proxy']['ports'])


def main():
    docker_compose = sys.argv[1]
    yaml = load_compose(docker_compose)
    update_proxy_port_mapping(yaml)
    with open(docker_compose, 'w') as f:
        dump_yaml(yaml, f)


if __name__ == '__main__':
    main()
