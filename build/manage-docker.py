#!/usr/bin/python
# PYTHON_ARGCOMPLETE_OK

# Copyright 2017, deepsense.ai
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
import collections
import os
import subprocess

import docker

cwd = os.path.join(os.path.dirname(os.path.realpath(__file__)), '..')

SimpleCommandConfig = collections.namedtuple('SimpleCommandConfig', 'docker_image_name type command')
SbtDockerConfig = collections.namedtuple('SbtDockerConfig', 'docker_image_name type sbt_task')

simple_command_type = 'simple_command'
sbt_type = 'sbt'


spark_version = "2.1.1"
hadoop_version = "2.7"


# This is added here since sbt clean doesn't clean everything; in particular, it doesn't clean
# project/target, so we delete all "target". For discussion, see
# http://stackoverflow.com/questions/4483230/an-easy-way-to-get-rid-of-everything-generated-by-sbt
# and
# https://github.com/sbt/sbt/issues/896
sbt_clean_more_cmd = "(find . -name target -type d -exec rm -rf {} \; || true) && sbt clean &&"


def simple_docker(docker_image_name, docker_file_path):
    tag = "{}:{}".format(docker_image_name, git_sha())
    command = "(cd {}; docker build -t {} .)".format(docker_file_path, tag)
    return SimpleCommandConfig(docker_image_name, simple_command_type, command)


# Currently passing --build-arg undefined in dockerfile fails the build.
# https://github.com/docker/docker/issues/26249
# Pass it everytime once issue is resolved
def simple_docker_with_spark_version(docker_image_name, docker_file_path):
    tag = "{}:{}".format(docker_image_name, git_sha())
    command = "(cd {}; docker build --build-arg SPARK_VERSION={} --build-arg HADOOP_VERSION={} -t {} .)".format(
        docker_file_path, spark_version, hadoop_version, tag)
    return SimpleCommandConfig(docker_image_name, simple_command_type, command)


def simple_command_docker(docker_image_name, command):
    return SimpleCommandConfig(docker_image_name, simple_command_type, command)


def sbt_docker(docker_image_name, project_name):
    return SbtDockerConfig(docker_image_name, sbt_type, "{}/docker".format(project_name))


def git_sha():
    sha_output_with_endline = subprocess.check_output("git rev-parse HEAD", shell=True, cwd=cwd)
    return sha_output_with_endline.strip()


image_confs = [
    simple_docker("seahorse-proxy", "proxy"),
    simple_docker("seahorse-rabbitmq", "deployment/rabbitmq"),
    simple_docker("seahorse-h2", "deployment/h2-docker"),
    simple_docker_with_spark_version("seahorse-spark", "deployment/spark-docker"),
    simple_command_docker("seahorse-mesos-spark", "./build/build_spark_docker_mesos.sh"),
    sbt_docker("seahorse-schedulingmanager", "schedulingmanager"),
    sbt_docker('seahorse-sessionmanager', "sessionmanager"),
    sbt_docker("seahorse-workflowmanager", "workflowmanager"),
    sbt_docker("seahorse-datasourcemanager", "datasourcemanager"),
    sbt_docker("seahorse-libraryservice", "libraryservice"),
    simple_docker("seahorse-notebooks", "remote_notebook"),
    simple_docker("seahorse-authorization", "deployment/authorization-docker"),
    simple_docker("seahorse-mail", "deployment/exim"),
    simple_command_docker("seahorse-frontend", "frontend/docker/build-frontend.sh")
]
image_conf_by_name = {conf.docker_image_name: conf for conf in image_confs}


def main():
    parser = argparse.ArgumentParser(description='Interface for docker manager',
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('-i', '--images',
                        nargs='+',
                        help='List of docker image',
                        action='store')
    parser.add_argument('--all',
                        help='If used, the script will work for all docker images',
                        action='store_true')
    parser.add_argument('-b', '--build',
                        help='Build docker images',
                        action='store_true')

    try:
        import argcomplete
        argcomplete.autocomplete(parser)
    except ImportError:
        print("Argcomplete is not installed. <tab> autocompletions are not available")
        print("Setup instructions at https://argcomplete.readthedocs.io/en/latest/")
    args, extra_args = parser.parse_known_args()

    if args.all:
        selected_confs = image_confs
    else:
        user_provided_images = args.images
        check_images_provided_by_user(user_provided_images)
        selected_confs = [image_conf_by_name.get(image) for image in args.images]

    if args.build:
        build_dockers(selected_confs)


def build_dockers(docker_configurations):
    simple_command_confs = [conf for conf in docker_configurations if conf.type == simple_command_type]
    for conf in simple_command_confs:
        print(conf.command)
        subprocess.call(conf.command, shell=True, cwd=cwd)

    sbt_confs = [conf for conf in docker_configurations if conf.type == sbt_type]
    if sbt_confs:
        sbt_commands = [conf.sbt_task for conf in sbt_confs]
        batched_sbt_tasks = ' '.join(sbt_commands)
        final_sbt_command = sbt_clean_more_cmd + "sbt -DSPARK_VERSION={} {}".format(spark_version, batched_sbt_tasks)
        print(final_sbt_command)
        subprocess.call(final_sbt_command, shell=True, cwd=cwd)


def check_images_provided_by_user(user_provided_images):
    for image in user_provided_images:
        if image_conf_by_name.get(image) is None:
            all_images = image_conf_by_name.keys()
            raise ValueError("Image {} is illegal. Possible values are {}".format(image, all_images))


if __name__ == '__main__':
    main()
