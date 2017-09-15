# Seahorse SDK usage example

## Getting started
You can clone this repository and create your own operations
(if this isn't root of a git project, you need to create one
- this won't be needed when SDK/seahorse-workflow-executor is published on MVNRepository)
When you're finished, create a JAR by running

`sbt assembly`

By default, Scala standard library and Seahorse are not included.
This example produces ~5kB JAR
(it can be found at: target/scala-2.11/seahorse-sdk-example-assembly-1.0.jar).
