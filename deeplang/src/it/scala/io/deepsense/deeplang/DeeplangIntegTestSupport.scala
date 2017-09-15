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
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar._

import io.deepsense.commons.models.Id
import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.deeplang.OperationExecutionDispatcher.Result
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.inference.InferContext


/**
 * Adds features to facilitate integration testing using Spark
 */
trait DeeplangIntegTestSupport extends UnitSpec with BeforeAndAfterAll {

  val testsDir = "target/tests"
  val absoluteTestsDirPath = new java.io.File(testsDir).getAbsoluteFile.toString
  var commonExecutionContext: CommonExecutionContext = _
  implicit var executionContext: ExecutionContext = _

  val sparkConf: SparkConf = DeeplangIntegTestSupport.sparkConf
  val sparkContext: SparkContext = DeeplangIntegTestSupport.sparkContext
  val sqlContext: SQLContext = DeeplangIntegTestSupport.sqlContext
  var fileSystemClient: FileSystemClient = _

  val dOperableCatalog = {
    val catalog = new DOperableCatalog
    CatalogRecorder.registerDOperables(catalog)
    catalog
  }

  override def beforeAll(): Unit = {
    fileSystemClient = LocalFileSystemClient()
    commonExecutionContext = prepareCommonExecutionContext()
    executionContext = prepareExecutionContext()
  }

  protected def prepareCommonExecutionContext(): CommonExecutionContext = {
    val inferContext = InferContext(
      DataFrameBuilder(sqlContext),
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser])

    new MockedCommonExecutionContext(
      sparkContext,
      sqlContext,
      inferContext,
      fileSystemClient,
      "testTenantId",
      mock[InnerWorkflowExecutor],
      mock[DataFrameStorage],
      mock[CustomCodeExecutionProvider])
  }

  protected def prepareExecutionContext(): ExecutionContext = {
    val inferContext = InferContext(
      DataFrameBuilder(sqlContext),
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser])

    new MockedExecutionContext(
      sparkContext,
      sqlContext,
      inferContext,
      fileSystemClient,
      "testTenantId",
      mock[InnerWorkflowExecutor],
      mock[ContextualDataFrameStorage],
      new MockedContextualCodeExecutor)
  }

  protected def createDataFrame(rows: Seq[Row], schema: StructType): DataFrame = {
    val rdd: RDD[Row] = sparkContext.parallelize(rows)
    val sparkDataFrame = sqlContext.createDataFrame(rdd, schema)
    DataFrame.fromSparkDataFrame(sparkDataFrame)
  }

  def executeOperation(op: DOperation, dfs: DataFrame*): DataFrame =
    op.execute(executionContext)(dfs.toVector).head.asInstanceOf[DataFrame]

  def createDir(path: String): Unit = {
    new java.io.File(path + "/id").getParentFile.mkdirs()
  }

  def createDataFrame[T <: Product : TypeTag : ClassTag](seq: Seq[T]): DataFrame = {
    DataFrame.fromSparkDataFrame(
      sqlContext.createDataFrame(sparkContext.parallelize(seq)))
  }

}

object DeeplangIntegTestSupport extends UnitSpec {
  val sparkConf: SparkConf = new SparkConf()
    .setMaster("local[4]")
    .setAppName("TestApp")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .registerKryoClasses(Array())
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  UserDefinedFunctions.registerFunctions(sqlContext.udf)

  def assertDataFramesEqual(
      actualDf: DataFrame,
      expectedDf: DataFrame,
      checkRowOrder: Boolean = true,
      checkNullability: Boolean = true): Unit = {
    // Checks only semantic identity, not objects location in memory
    assertSchemaEqual(
      actualDf.sparkDataFrame.schema, expectedDf.sparkDataFrame.schema, checkNullability)
    val collectedRows1: Array[Row] = actualDf.sparkDataFrame.collect()
    val collectedRows2: Array[Row] = expectedDf.sparkDataFrame.collect()
    if (checkRowOrder) {
      collectedRows1 shouldBe collectedRows2
    } else {
      collectedRows1 should contain theSameElementsAs collectedRows2
    }
  }

  def assertSchemaEqual(
      actualSchema: StructType,
      expectedSchema: StructType,
      checkNullability: Boolean): Unit = {
    val (actual, expected) = if (checkNullability) {
      (actualSchema, expectedSchema)
    } else {
      val actualNonNull = StructType(actualSchema.map(_.copy(nullable = false)))
      val expectedNonNull = StructType(expectedSchema.map(_.copy(nullable = false)))
      (actualNonNull, expectedNonNull)
    }
    assertSchemaEqual(actual, expected)
  }

  def assertSchemaEqual(actualSchema: StructType, expectedSchema: StructType): Unit = {
    actualSchema.treeString shouldBe expectedSchema.treeString
  }
}

private class MockedCommonExecutionContext(
    override val sparkContext: SparkContext,
    override val sqlContext: SQLContext,
    override val inferContext: InferContext,
    override val fsClient: FileSystemClient,
    override val tenantId: String,
    override val innerWorkflowExecutor: InnerWorkflowExecutor,
    override val dataFrameStorage: DataFrameStorage,
    override val customCodeExecutionProvider: CustomCodeExecutionProvider)
  extends CommonExecutionContext(
    sparkContext,
    sqlContext,
    inferContext,
    fsClient,
    "/tmp",
    tenantId,
    innerWorkflowExecutor,
    dataFrameStorage,
    customCodeExecutionProvider) {

  override def createExecutionContext(workflowId: Id, nodeId: Id): ExecutionContext =
    new MockedExecutionContext(sparkContext,
      sqlContext,
      inferContext,
      fsClient,
      tenantId,
      innerWorkflowExecutor,
      mock[ContextualDataFrameStorage],
      new MockedContextualCodeExecutor)
}

private class MockedExecutionContext(
    override val sparkContext: SparkContext,
    override val sqlContext: SQLContext,
    override val inferContext: InferContext,
    override val fsClient: FileSystemClient,
    override val tenantId: String,
    override val innerWorkflowExecutor: InnerWorkflowExecutor,
    override val dataFrameStorage: ContextualDataFrameStorage,
    override val customCodeExecutor: ContextualCustomCodeExecutor)
  extends ExecutionContext(
    sparkContext,
    sqlContext,
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
  extends CustomCodeExecutionProvider(new MockedCodeExecutor, new MockedCustomOperationExecutor)

private class MockedContextualCodeExecutor
  extends ContextualCustomCodeExecutor(
    new MockedCustomCodeExecutionProvider, Id.randomId, Id.randomId)

private class MockedCustomOperationExecutor
  extends OperationExecutionDispatcher {
  override def executionStarted(workflowId: Id, nodeId: Id): Future[Result] =
    Future.successful(Right(()))
}
