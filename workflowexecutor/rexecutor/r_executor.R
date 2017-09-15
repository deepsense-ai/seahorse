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

.libPaths(c(file.path("/opt/spark-1.6.1/R/lib/"), .libPaths()))
library(SparkR)

SparkR:::connectBackend("localhost", backendPort)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign(".sc", SparkR:::callJMethod(entryPoint, "getSparkContext"), envir = SparkR:::.sparkREnv)
assign("sc", get(".sc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
assign(".sqlc", SparkR:::callJMethod(entryPoint, "getSqlContext"), envir = SparkR:::.sparkREnv)
assign("sqlContext", get(".sqlc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
sdf <- SparkR:::callJMethod(entryPoint, "retrieveInputDataFrame", workflowId, nodeId, as.integer(0))
df <- SparkR:::dataFrame(sdf, isCached = FALSE)


tryCatch({
  eval(parse(text = code))
  transformedDF <- transform(df)
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

