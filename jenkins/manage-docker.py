#!/usr/bin/python

# Copyright (c) 2016, CodiLime Inc.

import argparse
import collections
import os
import subprocess

cwd = os.path.join(os.path.dirname(os.path.realpath(__file__)), '..')
DockerImageConfig = collections.namedtuple('DockerImageConfig', 'docker_image_name build_script')


def build_simple_docker(docker_file_path, project_name):
    return "./jenkins/scripts/build-local-docker.sh {} {}".format(docker_file_path, project_name)


def build_sbt_docker(project_name):
    return "sbt clean && sbt {}/docker:publishLocal".format(project_name)


image_confs = [
    DockerImageConfig("deepsense-proxy", build_simple_docker("proxy", "deepsense-proxy")),
    DockerImageConfig("deepsense-rabbitmq", build_simple_docker("deployment/rabbitmq", "deepsense-rabbitmq")),
    DockerImageConfig("deepsense-h2", build_simple_docker("deployment/h2-docker", "deepsense-h2")),
    DockerImageConfig("deepsense-spark", build_simple_docker("deployment/spark-docker", "deepsense-spark")),
    DockerImageConfig("deepsense-mesos-spark", "./jenkins/build_spark_docker_mesos.sh"),
    DockerImageConfig("deepsense-schedulingmanager", build_sbt_docker("schedulingmanager")),
    DockerImageConfig("deepsense-sessionmanager", build_sbt_docker("sessionmanager")),
    DockerImageConfig("deepsense-workflowmanager", build_sbt_docker("workflowmanager")),
    DockerImageConfig("deepsense-datasourcemanager", build_sbt_docker("datasourcemanager")),
    DockerImageConfig("deepsense-libraryservice", build_sbt_docker("libraryservice")),
    DockerImageConfig("deepsense-notebooks", build_simple_docker("remote_notebook", "deepsense-notebooks")),
    DockerImageConfig("deepsense-authorization",
                      build_simple_docker("deployment/authorization-docker", "deepsense-authorization")),
    DockerImageConfig("deepsense-mail",
                      build_simple_docker("deployment/exim", "deepsense-mail"))
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

    for conf in selected_confs:
        if args.sync:
            print "Syncing {} image".format(conf.docker_image_name)
            check_if_git_repo_is_clean()
            pull_cmd = "docker pull {}/{}:{}".format(docker_repo, conf.docker_image_name, git_sha())
            pull_code = subprocess.call(pull_cmd, shell=True, cwd=cwd)
            if pull_code > 0:
                print "Docker image miss in registry. Building local docker from scratch..."
                subprocess.call(conf.build_script, shell=True, cwd=cwd)
        if args.build:
            print "Building {} image".format(conf.docker_image_name)
            subprocess.call(conf.build_script, shell=True, cwd=cwd)
        if args.publish:
            print "Publishing {} image".format(conf.docker_image_name)
            script = "./jenkins/scripts/publish-local-docker.sh {}".format(conf.docker_image_name)
            subprocess.call(script, shell=True, cwd=cwd)

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

def git_sha():
    return subprocess.check_output("git rev-parse HEAD", shell=True, cwd=cwd)

def check_images_provided_by_user(user_provided_images):
    for image in user_provided_images:
        if image_conf_by_name.get(image) is None:
            all_images = image_conf_by_name.keys()
            raise ValueError("Image {} is illegal. Possible values are {}".format(image, all_images))


if __name__ == '__main__':
    main()
