#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Publish docker images from codilime repository to quay
# Copy Vagrantfile, Vagrant box, docker-compose.yml and worflowexecutor.jar to S3
# $SEAHORSE_BUILD_TAG required for deployment
# $API_VERSION
# $RELEASE_TO_S3

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"
API_VERSION="${API_VERSION?Need to set API_VERSION. For example setting it to 1.3.0}"
RELEASE_TO_S3="${RELEASE_TO_S3?Need to set RELEASE_TO_S3 to \"false\" for not releasing to S3. Set RELEASE_TO_S3 to anything else for releasing to S3.}"


# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

RELEASE_PATH=workflowexecutor/seahorse/releases/${API_VERSION}
WE_RELEASE_PATH=workflowexecutor/releases/${API_VERSION}

AWS_FILES_COUNT=`aws s3 ls s3://${RELEASE_PATH}/ | wc -l`
if [ $AWS_FILES_COUNT -ne 0 ]; then
  echo "Release directory ${RELEASE_PATH} already exists on S3 and is not empty, aborting"
  aws s3 ls s3://${RELEASE_PATH}/
  exit -1
fi

ARTIFACTORY_CREDENTIALS=$HOME/.artifactory_credentials
ARTIFACTORY_URL=`grep "host=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`

SEAHORSE_WORKFLOWEXECUTOR_REPOSITORY="seahorse-workflowexecutor"
SEAHORSE_DISTRIBUTION_REPOSITORY="seahorse-distribu tion"

echo "Publish docker images to quay.io and generate docker-compose.yml"
./jenkins/release_docker_images_to_quay.sh ${ARTIFACTORY_URL} ${SEAHORSE_DISTRIBUTION_REPOSITORY} ${SEAHORSE_BUILD_TAG}

if [ $RELEASE_TO_S3 = "false" ]
then
  echo "RELEASE_TO_S3 equals to $RELEASE_TO_S3, finishing job by skipping Releasing to S3"
  exit 0
fi
echo "RELEASE_TO_S3 equals to $RELEASE_TO_S3, continue job and Release artifacts to S3"


echo "Publish to S3 workflowexecutor uber-jars"
rm -f workflowexecutor.jar

wget "${ARTIFACTORY_URL}/${SEAHORSE_WORKFLOWEXECUTOR_REPOSITORY}/io/deepsense/deepsense-seahorse-workflowexecutor_2.11/${SEAHORSE_BUILD_TAG}/workflowexecutor.jar"
aws s3 cp workflowexecutor.jar s3://${WE_RELEASE_PATH}/workflowexecutor_2.11-${API_VERSION}.jar --acl public-read

DOCKER_COMPOSE_YML="docker-compose.yml"
echo "Publish to S3 $DOCKER_COMPOSE_YML"
rm -f $DOCKER_COMPOSE_YML
wget "${ARTIFACTORY_URL}/${SEAHORSE_DISTRIBUTION_REPOSITORY}/io/deepsense/${SEAHORSE_BUILD_TAG}/dockercompose/$DOCKER_COMPOSE_YML"
aws s3 cp $DOCKER_COMPOSE_YML s3://${RELEASE_PATH}/$DOCKER_COMPOSE_YML --acl public-read


VAGRANTFILE="Vagrantfile"
echo "Generating and sending $VAGRANTFILE to artifactory"
rm -f $VAGRANTFILE
SEAHORSE_VM_BOX_FILE="seahorse-vm-${API_VERSION}.box"
URL="https://s3.amazonaws.com/${RELEASE_PATH}/${SEAHORSE_VM_BOX_FILE}"
sed -e "s#SEAHORSE_BOX_URL_VARIABLE#${URL}#" -e "s#SEAHORSE_BOX_NAME_VARIABLE#seahorse-vm-${API_VERSION}#" -e "s#SEAHORSE_BOX_HOSTNAME_VARIABLE#seahorse-vm-${API_VERSION//./-}#" deployment/image_publication/Vagrantfile.template > $VAGRANTFILE

source jenkins/publish_to_artifactory_function.sh
publish_to_artifactory $VAGRANTFILE ${SEAHORSE_DISTRIBUTION_REPOSITORY}/io/deepsense/${SEAHORSE_BUILD_TAG}/vagrant/${VAGRANTFILE}


echo "Publish to S3 $VAGRANTFILE"
aws s3 cp $VAGRANTFILE s3://${RELEASE_PATH}/$VAGRANTFILE --acl public-read

echo "Publish to S3 $SEAHORSE_VM_BOX_FILE"
rm -f $SEAHORSE_VM_BOX_FILE
wget "${ARTIFACTORY_URL}/${SEAHORSE_DISTRIBUTION_REPOSITORY}/io/deepsense/${SEAHORSE_BUILD_TAG}/vagrant/$SEAHORSE_VM_BOX_FILE"
aws s3 cp $SEAHORSE_VM_BOX_FILE s3://${RELEASE_PATH}/$SEAHORSE_VM_BOX_FILE --acl public-read
