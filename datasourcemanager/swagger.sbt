enablePlugins(ScalatraSwaggerCodegenPlugin)

val swaggerJsonPath = "seahorse-workflow-executor/api/src/main/resources/datasourcemanager.swagger.json"

swaggerSpecPath := swaggerJsonPath
generatedCodePackage := "ai.deepsense.seahorse.datasource"

resourceGenerators in Compile += copySwaggerJsonForStaticServing().taskValue

def copySwaggerJsonForStaticServing() = Def.task {
  val swaggerInSeahorseWorkflowExecutor = baseDirectory.value / ".." / swaggerJsonPath
  val swaggerServedForSwaggerUI = (resourceManaged in Compile).value / "scalatra-webapp/swagger.json"
  IO.copyFile(swaggerInSeahorseWorkflowExecutor, swaggerServedForSwaggerUI)
  Seq(swaggerServedForSwaggerUI)
}