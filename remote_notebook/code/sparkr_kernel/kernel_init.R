entryPointId <- "0"

.libPaths(c(file.path("/opt/spark-2.0.0/R/lib/"), .libPaths()))
library(SparkR)

SparkR:::connectBackend(r_backend_host, r_backend_port)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign(".sc", SparkR:::callJMethod(entryPoint, "getSparkContext"), envir = SparkR:::.sparkREnv)
assign("sc", get(".sc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
assign(".sqlc", SparkR:::callJMethod(entryPoint, "getSqlContext"), envir = SparkR:::.sparkREnv)
assign("sqlContext", get(".sqlc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)

dataframe <- function() {
    if (!exists("workflow_id") || !exists("node_id") || !exists("port_number")) {
        stop("No edge is connected to this Notebook")
    }

    sdf <- tryCatch({
        SparkR:::callJMethod(entryPoint, "retrieveOutputDataFrame",
                             toString(workflow_id), toString(node_id), as.integer(port_number))
    }, error = function(err) {
        stop("Input operation is not yet executed")
    })

    SparkR:::dataFrame(sdf, isCached = FALSE)
}
