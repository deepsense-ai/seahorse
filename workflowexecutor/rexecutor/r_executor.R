args <- commandArgs(trailingOnly = TRUE)
sparkVersion <- args[1]
backendPort <- as.integer(args[2])
workflowId <- args[3]
nodeId <- args[4]
entryPointId <- args[5]
code <- URLdecode(args[6])

print(sparkVersion)
print(backendPort)
print(workflowId)
print(nodeId)
print(entryPointId)
print(code)

rm(args)

sparkDFName <- ifelse(sparkVersion == "2.0.0", "SparkDataFrame", "DataFrame")

# R will install packages to first lib path in here. We will mount it as docker volume to persist packages.
.libPaths(c(file.path("/opt/R_Libs"), file.path(Sys.getenv('SPARK_HOME'), 'R', 'lib'), .libPaths()))
library(SparkR)

SparkR:::connectBackend("localhost", backendPort)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign(".sparkRjsc", SparkR:::callJMethod(entryPoint, "getSparkContext"), envir = SparkR:::.sparkREnv)
assign("sc", get(".sparkRjsc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)

sparkSQLSession <- SparkR:::callJMethod(entryPoint, "getNewSparkSQLSession")

if (sparkVersion == "2.0.0") {
  assign(".sparkRsession", SparkR:::callJMethod(sparkSQLSession, "getSparkSession"), envir = SparkR:::.sparkREnv)
  assign("spark", get(".sparkRsession", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
} else {
  assign(".sqlc", SparkR:::callJMethod(sparkSQLSession, "getSQLContext"), envir = SparkR:::.sparkREnv)
  assign("sqlContext", get(".sqlc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
  assign("spark", get(".sqlc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
}

sdf <- SparkR:::callJMethod(entryPoint, "retrieveInputDataFrame", workflowId, nodeId, as.integer(0))
df <- SparkR:::dataFrame(SparkR:::callJMethod(spark,
                                              "createDataFrame",
                                              SparkR:::callJMethod(sdf, "rdd"),
                                              SparkR:::callJMethod(sdf, "schema")))

tryCatch({
  eval(parse(text = code))
  transformedDF <- transform(df)
  if (class(transformedDF) != sparkDFName) {
    transformedDF <- ifelse(
      sparkVersion == "2.0.0",
      createDataFrame(data.frame(transformedDF)),
      createDataFrame(data.frame(spark, transformedDF)))
  }

  SparkR:::callJMethod(entryPoint, "registerOutputDataFrame", workflowId, nodeId, as.integer(0), transformedDF@sdf)
  SparkR:::callJMethod(entryPoint, "executionCompleted", workflowId, nodeId)
}, error = function(err) {
      SparkR:::callJMethod(entryPoint, "executionFailed", workflowId, nodeId,  paste(err))
})


valid <- ls(SparkR:::.validJobjs)
for(objId in valid) {
 # customCodeEntryPoint shouldn't be removed
 if(objId != entryPointId) {
   SparkR:::removeJObject(objId)
 }
}

rm(list = valid, envir = SparkR:::.validJobjs)
