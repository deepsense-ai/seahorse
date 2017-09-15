#!/usr/bin/env bash
cd `dirname $0`"/../"
sbt "scalafmt-stub/compile:runMain org.scalafmt.cli.Cli $@"