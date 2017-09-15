enablePlugins(ScalatraSwaggerCodegenPlugin)

val swaggerJsonPath = "seahorse-workflow-executor/api/src/main/resources/io/deepsense/datasourcemanager/swagger.json"

swaggerSpecPath := swaggerJsonPath
generatedCodePackage := "io.deepsense.seahorse.datasource"

resourceGenerators in Compile += copySwaggerJsonForStaticServing().taskValue

def copySwaggerJsonForStaticServing() = Def.task {
  val swaggerInSeahorseWorkflowExecutor = baseDirectory.value / ".." / swaggerJsonPath
  val swaggerServedForSwaggerUI = (resourceManaged in Compile).value / "scalatra-webapp/swagger.json"
  IO.copyFile(swaggerInSeahorseWorkflowExecutor, swaggerServedForSwaggerUI)
  Seq(swaggerServedForSwaggerUI)
}