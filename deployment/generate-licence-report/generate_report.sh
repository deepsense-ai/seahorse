#!/usr/bin/env bash

# Example usage `./generate_report.sh /home/adam/Src/deepsense-backend /home/adam/Src/ds-workflow_executor`

# Before running this script `sbt dumpLicenseReport` must be run in both backend and executor

# CHANGE TO YOUR PATHS
BACKEND=$1
EXECUTOR=$2

mkdir -p ./input

cp ${BACKEND}/*/target/license-reports/*.csv input/
cp ${EXECUTOR}/*/target/license-reports/*.csv input/

for file in ./input/*
do
  sed -i '1d' $file
done

cat ./input/*.csv | sort | uniq > report.csv
rm -rf ./input
