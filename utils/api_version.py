#!/usr/bin/env python

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
