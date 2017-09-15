#!/usr/bin/env python

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


def update_proxy_HOST(yaml):
    yaml['services']['proxy']['environment']['HOST'] = '0.0.0.0'


def main():
    docker_compose = sys.argv[1]
    yaml = load_compose(docker_compose)
    update_proxy_HOST(yaml)
    with open(docker_compose, 'w') as f:
        dump_yaml(yaml, f)


if __name__ == '__main__':
    main()
