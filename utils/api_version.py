#!/usr/bin/python

# Copyright (c) 2016, CodiLime Inc.

import os
import re


def get_sbt_setting_from_file(filename):
    with open(filename) as f:
        return f.read()


def read_api_version():
    dir_path = os.path.dirname(os.path.realpath(__file__))
    version_sbt = os.path.join(dir_path, '..', 'version.sbt')
    content = get_sbt_setting_from_file(version_sbt)
    return extract_api_version(content)


def extract_api_version(sbt_setting):
    return re.search('version in ThisBuild := "([0-9.]+(-RC[0-9]+)?).*"',
                     sbt_setting).group(1)


def main():
    print read_api_version()


if __name__ == '__main__':
    main()
