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

.libPaths(c(file.path("/opt/spark-2.0.0/R/lib/"), .libPaths()))
library(SparkR)

SparkR:::connectBackend("localhost", backendPort)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign("sc", SparkR:::callJMethod(entryPoint, "getSparkContext"), envir = .GlobalEnv)
assign("spark", SparkR:::callJMethod(entryPoint, "getSparkSession"), envir = .GlobalEnv)

sdf <- SparkR:::callJMethod(entryPoint, "retrieveInputDataFrame", workflowId, nodeId, as.integer(0))
df <- SparkR:::dataFrame(sdf, isCached = FALSE)

tryCatch({
  eval(parse(text = code))
  transformedDF <- transform(df)
  if (class(transformedDF) != "SparkDataFrame") {
    transformedDF <- createDataFrame(spark, data.frame(transformedDF))
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
