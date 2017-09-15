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

package io.deepsense.deeplang

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar._

import io.deepsense.commons.models.Id
import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.deeplang.OperationExecutionDispatcher.Result
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.utils.DataFrameMatchers
import io.deepsense.sparkutils.SparkSQLSession


/**
 * Adds features to facilitate integration testing using Spark
 */
trait DeeplangIntegTestSupport extends UnitSpec with BeforeAndAfterAll with LocalExecutionContext {

  protected def createDataFrame(rows: Seq[Row], schema: StructType): DataFrame = {
    val rdd: RDD[Row] = sparkContext.parallelize(rows)
    val sparkDataFrame = sparkSQLSession.createDataFrame(rdd, schema)
    DataFrame.fromSparkDataFrame(sparkDataFrame)
  }

  def executeOperation(op: DOperation, dfs: DataFrame*): DataFrame =
    op.executeUntyped(dfs.toVector)(executionContext).head.asInstanceOf[DataFrame]

  def createDir(path: String): Unit = {
    new java.io.File(path + "/id").getParentFile.mkdirs()
  }

  def createDataFrame[T <: Product : TypeTag : ClassTag](seq: Seq[T]): DataFrame = {
    DataFrame.fromSparkDataFrame(
      sparkSQLSession.createDataFrame(sparkContext.parallelize(seq)))
  }

}

object DeeplangIntegTestSupport extends UnitSpec with DataFrameMatchers {
  val sparkConf: SparkConf = new SparkConf()
    .setMaster("local[4]")
    .setAppName("TestApp")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .registerKryoClasses(Array())
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sparkSQLSession: SparkSQLSession = new SparkSQLSession(sparkContext)

  UserDefinedFunctions.registerFunctions(sparkSQLSession.udfRegistration)
}

private class MockedCommonExecutionContext(
    override val sparkContext: SparkContext,
    override val sparkSQLSession: SparkSQLSession,
    override val inferContext: InferContext,
    override val fsClient: FileSystemClient,
    override val tenantId: String,
    override val innerWorkflowExecutor: InnerWorkflowExecutor,
    override val dataFrameStorage: DataFrameStorage,
    override val customCodeExecutionProvider: CustomCodeExecutionProvider)
  extends CommonExecutionContext(
    sparkContext,
    sparkSQLSession,
    inferContext,
    fsClient,
    "/tmp",
    tenantId,
    innerWorkflowExecutor,
    dataFrameStorage,
    customCodeExecutionProvider) {

  override def createExecutionContext(workflowId: Id, nodeId: Id): ExecutionContext =
    new MockedExecutionContext(sparkContext,
      sparkSQLSession,
      inferContext,
      fsClient,
      tenantId,
      innerWorkflowExecutor,
      mock[ContextualDataFrameStorage],
      new MockedContextualCodeExecutor)
}

// TODO Unnecessary intermediate object. Remove.
private class MockedExecutionContext(
    override val sparkContext: SparkContext,
    override val sparkSQLSession: SparkSQLSession,
    override val inferContext: InferContext,
    override val fsClient: FileSystemClient,
    override val tenantId: String,
    override val innerWorkflowExecutor: InnerWorkflowExecutor,
    override val dataFrameStorage: ContextualDataFrameStorage,
    override val customCodeExecutor: ContextualCustomCodeExecutor)
  extends ExecutionContext(
    sparkContext,
    sparkSQLSession,
    inferContext,
    fsClient,
    "/tmp",
    tenantId,
    innerWorkflowExecutor,
    dataFrameStorage,
    customCodeExecutor)

private class MockedCodeExecutor extends CustomCodeExecutor {

  override def isValid(code: String): Boolean = true

  override def run(workflowId: String, nodeId: String, code: String): Unit = ()
}

private class MockedCustomCodeExecutionProvider
  extends CustomCodeExecutionProvider(
    new MockedCodeExecutor, new MockedCodeExecutor, new MockedCustomOperationExecutor)

private class MockedContextualCodeExecutor
  extends ContextualCustomCodeExecutor(
    new MockedCustomCodeExecutionProvider, Id.randomId, Id.randomId)

private class MockedCustomOperationExecutor
  extends OperationExecutionDispatcher {
  override def executionStarted(workflowId: Id, nodeId: Id): Future[Result] =
    Future.successful(Right(()))
}
