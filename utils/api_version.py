#!/usr/bin/python

# Copyright (c) 2016, CodiLime Inc.

import os
import re


def read_api_version():
    dir_path = os.path.dirname(os.path.realpath(__file__))
    version_sbt = os.path.join(dir_path, '..', 'version.sbt')
    with open(version_sbt) as f:
        content = f.read()
        return re.search('version in ThisBuild := "([0-9.]+).*"', content).group(1)


def main():
    print read_api_version()


if __name__ == '__main__':
    main()
