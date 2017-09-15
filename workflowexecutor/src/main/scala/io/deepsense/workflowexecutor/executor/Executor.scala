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

import io.deepsense.commons.BuildInfo
import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables.ReportLevel._
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.inference.InferContext

trait Executor extends Logging {

  def currentVersion: Version =
    Version(BuildInfo.apiVersionMajor, BuildInfo.apiVersionMinor, BuildInfo.apiVersionPatch)

  def createExecutionContext(
      reportLevel: ReportLevel,
      dataFrameStorage: DataFrameStorage,
      pythonExecutionCaretaker: PythonExecutionCaretaker,
      sparkContext: SparkContext,
      dOperableCatalog: Option[DOperableCatalog] = None): CommonExecutionContext = {

    val sqlContext = createSqlContext(sparkContext)
    val catalog = dOperableCatalog.getOrElse(createDOperableCatalog())

    val tenantId = ""

    val inferContext = InferContext(
      DataFrameBuilder(sqlContext),
      tenantId,
      catalog,
      fullInference = true)

    CommonExecutionContext(
      sparkContext,
      sqlContext,
      inferContext,
      FileSystemClientStub(), // temporarily mocked
      reportLevel,
      tenantId,
      dataFrameStorage,
      new PythonExecutionProvider {
        override def pythonCodeExecutor: PythonCodeExecutor =
          pythonExecutionCaretaker.pythonCodeExecutor

        override def customOperationExecutor: CustomOperationExecutor =
          pythonExecutionCaretaker.customOperationExecutor
      })
  }

  def createSparkContext(): SparkContext = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("Seahorse Workflow Executor")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array())

    new SparkContext(sparkConf)
  }

  def createSqlContext(sparkContext: SparkContext): SQLContext = {
    val sqlContext = new SQLContext(sparkContext)
    UserDefinedFunctions.registerFunctions(sqlContext.udf)
    sqlContext
  }

  def createDOperableCatalog(): DOperableCatalog = {
    val catalog = new DOperableCatalog
    CatalogRecorder.registerDOperables(catalog)
    catalog
  }

  def createDOperationsCatalog(): DOperationsCatalog = {
    val catalog = DOperationsCatalog()
    CatalogRecorder.registerDOperations(catalog)
    catalog
  }
}
