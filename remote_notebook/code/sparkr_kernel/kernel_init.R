entryPointId <- "0"

# R will install packages to first lib path in here. We will mount it as docker volume to persist packages.
.libPaths(c(file.path("/opt/R_Libs"), c(file.path("/opt/spark-2.0.0/R/lib/"), .libPaths()))
library(SparkR)

SparkR:::connectBackend(r_backend_host, r_backend_port)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign(".sparkRjsc", SparkR:::callJMethod(entryPoint, "getSparkSession"), envir = SparkR:::.sparkREnv)
assign("sc", get(".sparkRjsc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
assign(".sparkRsession", SparkR:::callJMethod(entryPoint, "getSparkSession"), envir = SparkR:::.sparkREnv)
assign("spark", get(".sparkRsession", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)

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
