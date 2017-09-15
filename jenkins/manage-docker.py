#!/usr/bin/python

# Copyright (c) 2016, CodiLime Inc.

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


def simple_command_docker(docker_image_name, command):
    return SimpleCommandConfig(docker_image_name, simple_command_type, command)


def sbt_docker(docker_image_name, project_name):
    return SbtDockerConfig(docker_image_name, sbt_type, "{}/docker".format(project_name))


def git_sha():
    sha_output_with_endline = subprocess.check_output("git rev-parse HEAD", shell=True, cwd=cwd)
    return sha_output_with_endline.strip()


image_confs = [
    simple_docker("deepsense-proxy", "proxy"),
    simple_docker("deepsense-rabbitmq", "deployment/rabbitmq"),
    simple_docker("deepsense-h2", "deployment/h2-docker"),
    simple_docker("deepsense-spark", "deployment/spark-docker"),
    simple_command_docker("deepsense-mesos-spark", "./jenkins/build_spark_docker_mesos.sh"),
    sbt_docker("deepsense-schedulingmanager", "schedulingmanager"),
    sbt_docker('deepsense-sessionmanager', "sessionmanager"),
    sbt_docker("deepsense-workflowmanager", "workflowmanager"),
    sbt_docker("deepsense-datasourcemanager", "datasourcemanager"),
    sbt_docker("deepsense-libraryservice", "libraryservice"),
    simple_docker("deepsense-notebooks", "remote_notebook"),
    simple_docker("deepsense-authorization", "deployment/authorization-docker"),
    simple_docker("deepsense-mail", "deployment/exim")
]
image_conf_by_name = {conf.docker_image_name: conf for conf in image_confs}
docker_repo = "docker-repo.deepsense.codilime.com/deepsense_io"

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
    parser.add_argument('-s', '--sync',
                        help=('Sync docker images with current git repo commit. ' +
                              'Syncing means pulling or building images for current git SHA.'),
                        action='store_true')
    parser.add_argument('-b', '--build',
                        help='Build docker images',
                        action='store_true')
    parser.add_argument('-p', '--publish',
                        help='Publish docker images',
                        action='store_true')

    args, extra_args = parser.parse_known_args()

    if args.all:
        selected_confs = image_confs
    else:
        user_provided_images = args.images
        check_images_provided_by_user(user_provided_images)
        selected_confs = [image_conf_by_name.get(image) for image in args.images]

    if args.build:
        build_dockers(selected_confs)

    if args.sync:
        check_if_git_repo_is_clean()

        images_missing_in_registry_confs = []
        for conf in selected_confs:
            pull_cmd = 'docker pull {}/{}:{}'.format(docker_repo, conf.docker_image_name, git_sha())
            pull_code = subprocess.call(pull_cmd, shell=True, cwd=cwd)
            if pull_code > 0:
                print 'Docker image missing in registry. Will build local docker from scratch...'
                images_missing_in_registry_confs.append(conf)
        build_dockers(images_missing_in_registry_confs)

    if args.publish:
        check_if_git_repo_is_clean()
        for conf in selected_confs:
            print 'Publishing {} image'.format(conf.docker_image_name)
            push_docker_with_base_sha_tag(conf)
            push_docker_with_seahorse_build_tag_if_defined(conf)
            push_docker_with_branch_latest_tag_if_on_branch(conf)

def build_dockers(docker_configurations):
    simple_command_confs = [conf for conf in docker_configurations if conf.type == simple_command_type]
    for conf in simple_command_confs:
        print(conf.command)
        subprocess.call(conf.command, shell=True, cwd=cwd)
        assign_base_sha_tag_to_locally_built_image(conf)

    sbt_confs = [conf for conf in docker_configurations if conf.type == sbt_type]
    if sbt_confs:
        sbt_commands = [conf.sbt_task for conf in sbt_confs]
        batched_sbt_tasks = ' '.join(sbt_commands)
        final_sbt_command = sbt_clean_more_cmd + "sbt {}".format(batched_sbt_tasks)
        print(final_sbt_command)
        subprocess.call(final_sbt_command, shell=True, cwd=cwd)
        for conf in sbt_confs:
            assign_base_sha_tag_to_locally_built_image(conf)

def push_docker_with_base_sha_tag(docker_configuration):
    docker.push(base_sha_tag(docker_configuration))


def push_docker_with_seahorse_build_tag_if_defined(docker_configuration):
    seahorse_build_tag = os.environ.get('SEAHORSE_BUILD_TAG')
    if seahorse_build_tag is not None:
        print("Seahorse build tag: " + seahorse_build_tag)
        docker_tag = tag_with_custom_label(docker_configuration, seahorse_build_tag)
        docker.tag(base_sha_tag(docker_configuration), docker_tag)
        docker.push(docker_tag)


def push_docker_with_branch_latest_tag_if_on_branch(docker_configuration):
    # Fetch changes from origin to make sure we have current origin/master
    subprocess.call("git fetch origin", shell=True, cwd=cwd)
    # TODO Automatically derive branches. Make it work with any dev_* branches
    for branch in ['master', 'seahorse_on_desktop', 'seahorse_on_tap', 'seahorse_on_bdu']:
        sha_for_tip_of_remote_branch_cmd = "git rev-parse origin/{}".format(branch)
        sha_for_tip_of_remote_branch = subprocess.check_output(sha_for_tip_of_remote_branch_cmd, shell=True, cwd=cwd).strip()
        is_this_tip_of_remote_branch = sha_for_tip_of_remote_branch == git_sha()
        if is_this_tip_of_remote_branch:
            tag = tag_with_custom_label(docker_configuration, branch + "-latest")
            docker.push(tag)


def assign_base_sha_tag_to_locally_built_image(docker_configuration):
    locally_built_docker_image = docker.find_image(docker_configuration.docker_image_name + ":" + git_sha())
    target_tag = base_sha_tag(docker_configuration)
    docker.tag(locally_built_docker_image, target_tag)


def base_sha_tag(docker_configuration):
    return tag_with_custom_label(docker_configuration, git_sha())


def tag_with_custom_label(docker_configuration, label):
    return "docker-repo.deepsense.codilime.com/deepsense_io/{}:{}".format(docker_configuration.docker_image_name, label)


def check_if_git_repo_is_clean():
    unstaged_files = subprocess.check_output("git status --porcelain", shell=True, cwd=cwd)
    repo_clean = not unstaged_files
    if not repo_clean:
        print "####################################"
        print "# Repository has unstaged files!"
        print "# Docker images are function of git commits."
        print "# Docker tags are git hashes."
        print "# If you have unstaged changes your git hash is not affected and output docker image would be undeterministic"
        print "# In order to use `sync` commit all changes first."
        print "# Aborting..."
        print "####################################"
        raise Exception("Cannot sync with unstaged files")


def check_images_provided_by_user(user_provided_images):
    for image in user_provided_images:
        if image_conf_by_name.get(image) is None:
            all_images = image_conf_by_name.keys()
            raise ValueError("Image {} is illegal. Possible values are {}".format(image, all_images))


if __name__ == '__main__':
    main()
