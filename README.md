# ${1:Seahorse}

## Content

This document can be useful for Seahorse development and deployment teams.

## Publishing to Artifactory repo

We publish to Artifactory repo SNAPSHOT version
`sbt clean "+ sPublish"`
NOTE: Adding `+` before sbt command results in executing command for all scala versions
      listed in CommonSettingsPlugin.scala (`projectSettings.crossScalaVersions`).
NOTE: Setting CommonSettingsPlugin.scala (`projectSettings.crossPaths`) to `true`
      will result in scala version postfixes adding to artifact names and 
      scala version specific subdirectories in `target` folder.

## Creating WorkflowExecutor jar

For scala 2.10:
`sbt clean workflowexecutor/assembly`
For scala 2.10 & 2.11:
`sbt clean "+ workflowexecutor/assembly"`

Now assembled jar can be found under path:
`workflowexecutor/target/workflowexecutor.jar`

NOTE: clean before assembly is necessary to purge resulting jar from results of previous assembly 
      attempts (interruption of assembly task could leave half-assembled corrupted jar that will be 
      assumed by assembly task as valid WorkflowExecutor jar)


