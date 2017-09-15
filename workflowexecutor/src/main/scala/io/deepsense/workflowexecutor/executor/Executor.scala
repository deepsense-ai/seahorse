/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.workflowexecutor.executor

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.ReportLevel._
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{CatalogRecorder, ExecutionContext}

trait Executor extends Logging {

  def createExecutionContext(reportLevel: ReportLevel): ExecutionContext = {
    val sparkContext = createSparkContext()
    val sqlContext = createSqlContext(sparkContext)

    val dOperableCatalog = new DOperableCatalog
    CatalogRecorder.registerDOperables(dOperableCatalog)

    val tenantId = ""

    val inferContext = InferContext(
      DataFrameBuilder(sqlContext),
      entityStorageClient = null,  // temporarily not used
      tenantId,
      dOperableCatalog,
      fullInference = true)

    ExecutionContext(
      sparkContext,
      sqlContext,
      inferContext,
      FileSystemClientStub(), // temporarily mocked
      reportLevel,
      tenantId)
  }

  private def createSparkContext(): SparkContext = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("Seahorse Workflow Executor")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array())

    new SparkContext(sparkConf)
  }

  private def createSqlContext(sparkContext: SparkContext): SQLContext = {
    val sqlContext = new SQLContext(sparkContext)
    UserDefinedFunctions.registerFunctions(sqlContext.udf)
    sqlContext
  }

}
