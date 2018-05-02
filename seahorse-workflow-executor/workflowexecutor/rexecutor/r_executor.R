args <- commandArgs(trailingOnly = TRUE)
backendPort <- as.integer(args[1])
workflowId <- args[2]
nodeId <- args[3]
entryPointId <- args[4]
code <- URLdecode(args[5])

print(backendPort)
print(workflowId)
print(nodeId)
print(entryPointId)
print(code)

rm(args)

# R will install packages to first lib path in here. We will mount it as docker volume to persist packages.
.libPaths(c(file.path("/opt/R_Libs"), file.path(Sys.getenv('SPARK_HOME'), 'R', 'lib'), .libPaths()))
library(SparkR)

SparkR:::connectBackend("localhost", backendPort, timeout=600)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign(".sparkRjsc", SparkR:::callJMethod(entryPoint, "getSparkContext"), envir = SparkR:::.sparkREnv)
assign("sc", get(".sparkRjsc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)

sparkSQLSession <- SparkR:::callJMethod(entryPoint, "getNewSparkSQLSession")

sparkVersion <- SparkR:::callJMethod(sc, "version")
if (sparkVersion %in% c("2.0.0", "2.0.1", "2.0.2", "2.1.0", "2.1.1", "2.2.0")) {
  assign(".sparkRsession", SparkR:::callJMethod(sparkSQLSession, "getSparkSession"), envir = SparkR:::.sparkREnv)
  assign("spark", get(".sparkRsession", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
} else {
  msg <- paste("Unhandled Spark Version:", sparkVersion)
  stop(msg)
}

sdf <- SparkR:::callJMethod(entryPoint, "retrieveInputDataFrame", workflowId, nodeId, as.integer(0))
df <- SparkR:::dataFrame(SparkR:::callJMethod(spark,
                                              "createDataFrame",
                                              SparkR:::callJMethod(sdf, "rdd"),
                                              SparkR:::callJMethod(sdf, "schema")))

tryCatch({
  eval(parse(text = code))
  transformedDF <- transform(df)
  if (sparkVersion %in% c("2.0.0", "2.0.1", "2.0.2", "2.1.0", "2.1.1", "2.2.0")) {
    if (class(transformedDF) != "SparkDataFrame") {
      transformedDF <- createDataFrame(data.frame(transformedDF))
    }
  } else {
    msg <- paste("Unhandled Spark Version:", sparkVersion)
    stop(msg)
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
