#!/usr/bin/env bash
cd `dirname $0`"/../"
# This is equivalent to sbt "scalafmt $@". That shorter version doesn't always work, however,
# and for some projects (seahorse-workflow-executor) behaves incorrectly, as it calls
# scalafmt-stub/it:runMain (...), resulting in wrong path and not formatting anything.
sbt "scalafmt-stub/compile:runMain org.scalafmt.cli.Cli $@"