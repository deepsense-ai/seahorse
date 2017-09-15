#!/bin/bash -ex

sbt clean scalastyle test:scalastyle test it:compile ds-it