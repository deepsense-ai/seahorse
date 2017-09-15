#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish some artifacts from seahorse-workflow-executor on Sonatype/Central.
# User can use their own credentials or use ones provided by script. Artifacts can be either published only (in which
# case they don't go to Central) or released. Only non-snapshot versions can be released.
#
# WARNING: RELEASING IS A NON-REVERSIBLE OPERATION!
#
# Example usage:
# ./jenkins/publish_we_to_sonatype.sh [--setup-credentials] [--release]
# When running without --setup-credentials, it assumes that you have proper signing and publishing credentials
# in ~/.sbt

SETUP_CREDENTIALS=NO
RELEASE=NO
for i in "$@"
do
case $i in
  --setup-credentials)
  SETUP_CREDENTIALS=YES
  ;;
  --release)
  RELEASE=YES
  ;;
  --help)
  echo "Usage: $0 [--setup-credentials] [--release]"
  exit 0
  ;;
  *)
  echo "Invalid argument: $i. Usage: $0 [--setup-credentials] [--release]"
  exit 1
  ;;
esac
done

publish () {
  # Parent directory relative to script's.
  DEEPSENSE_BACKEND_ROOT=`dirname $0`"/../"
  cd $DEEPSENSE_BACKEND_ROOT

  if [ $SETUP_CREDENTIALS == YES ]
    then
      mkdir -p ~/.sbt/0.13/plugins
      mkdir -p ~/.sbt/gpg
      cp credentials/pubring.asc ~/.sbt/gpg/deepsense-pubring.asc
      cp credentials/secring.asc ~/.sbt/gpg/deepsense-secring.asc
      cp credentials/sonatype.sbt ~/.sbt/0.13
      cp credentials/deepsensePgp.sbt ~/.sbt/0.13
  fi

  cd ./seahorse-workflow-executor/
  sbt clean
  sbt publishSigned

  if [ $RELEASE == YES ]
    then
      # We should not release if it is a snapshot. Snapshot wouldn't be published anyway, but some previous version
      # might, which would be confusing. I could use "sbt isSnapshot" here, which would be more straightforward but
      # on the other hand, string "true" is more likely to show up in some subproject name than "SNAPSHOT".
      [[ $(sbt version) != *SNAPSHOT* ]] || { echo "Snapshots cannot be released!"; exit 1; }
      sbt sonatypeRelease
  fi
  exit 0
}

cleanup () {
  if [ $SETUP_CREDENTIALS == YES ]
    then
      # There is no need to clean up plugin.
      rm -f ~/.sbt/gpg/deepsense-secring.asc \
            ~/.sbt/gpg/deepsense-pubring.asc \
            ~/.sbt/0.13/sonatype.sbt \
            ~/.sbt/0.13/deepsensePgp.sbt
  fi
}

trap cleanup INT TERM EXIT
publish
trap - INT TERM EXIT
