entryPointId <- "0"

# R will install packages to first lib path in here. We will mount it as docker volume to persist packages.
.libPaths(c(file.path("/opt/R_Libs"), file.path(Sys.getenv('SPARK_HOME'), 'R', 'lib'), .libPaths()))
library(SparkR)

SparkR:::connectBackend(r_backend_host, r_backend_port)

assign(".scStartTime", as.integer(Sys.time()), envir = SparkR:::.sparkREnv)

entryPoint <- SparkR:::getJobj(entryPointId)

assign(".sparkRjsc", SparkR:::callJMethod(entryPoint, "getSparkContext"), envir = SparkR:::.sparkREnv)
assign("sc", get(".sparkRjsc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)

sparkSQLSession <- SparkR:::callJMethod(entryPoint, "getNewSparkSQLSession")

sparkVersion <- SparkR:::callJMethod(sc, "version")
if (sparkVersion == "2.0.0") {
  assign(".sparkRsession", SparkR:::callJMethod(sparkSQLSession, "getSparkSession"), envir = SparkR:::.sparkREnv)
  assign("spark", get(".sparkRsession", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
} else {
  assign(".sqlc", SparkR:::callJMethod(sparkSQLSession, "getSQLContext"), envir = SparkR:::.sparkREnv)
  assign("sqlContext", get(".sqlc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
  assign("spark", get(".sqlc", envir = SparkR:::.sparkREnv), envir = .GlobalEnv)
}

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

    SparkR:::dataFrame(SparkR:::callJMethod(spark,
                                            "createDataFrame",
                                            SparkR:::callJMethod(sdf, "rdd"),
                                            SparkR:::callJMethod(sdf, "schema")))
}
