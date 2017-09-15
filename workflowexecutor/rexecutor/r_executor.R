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
.libPaths(c(file.path("/opt/R_Libs"), c(file.path("/opt/spark-2.0.0/R/lib/"), .libPaths()))
library(SparkR)

SparkR:::connectBackend("localhost", backendPort)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign(".sparkRjsc", SparkR:::callJMethod(entryPoint, "getSparkSession"), envir = SparkR:::.sparkREnv)
assign("sc", get(".sparkRjsc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
assign(".sparkRsession", SparkR:::callJMethod(entryPoint, "getSparkSession"), envir = SparkR:::.sparkREnv)
assign("spark", get(".sparkRsession", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)

sdf <- SparkR:::callJMethod(entryPoint, "retrieveInputDataFrame", workflowId, nodeId, as.integer(0))
df <- SparkR:::dataFrame(sdf, isCached = FALSE)

tryCatch({
  eval(parse(text = code))
  transformedDF <- transform(df)
  if (class(transformedDF) != "SparkDataFrame") {
    transformedDF <- createDataFrame(data.frame(transformedDF))
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
