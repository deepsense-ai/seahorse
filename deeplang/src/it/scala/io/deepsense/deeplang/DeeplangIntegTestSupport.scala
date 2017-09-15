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

import java.util.UUID

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll

import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.deeplang.doperables.ReportLevel
import io.deepsense.deeplang.doperables.ReportLevel._
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.entitystorage.EntityStorageClientInMemoryImpl
import io.deepsense.models.entities.Entity

/**
 * Adds features to facilitate integration testing using Spark and entitystorage
 */
trait DeeplangIntegTestSupport extends UnitSpec with BeforeAndAfterAll {

  val testsDir = "target/tests"
  val absoluteTestsDirPath = new java.io.File(testsDir).getAbsoluteFile.toString
  var executionContext: ExecutionContext = _

  val sparkConf: SparkConf = DeeplangIntegTestSupport.sparkConf
  val sparkContext: SparkContext = DeeplangIntegTestSupport.sparkContext
  val sqlContext: SQLContext = DeeplangIntegTestSupport.sqlContext
  var fileSystemClient: FileSystemClient = _

  override def beforeAll(): Unit = {
    fileSystemClient = LocalFileSystemClient()
    executionContext = prepareExecutionContext()
  }

  protected def prepareExecutionContext(): ExecutionContext = {
    val inferContext = InferContext(
      DataFrameBuilder(sqlContext),
      EntityStorageClientInMemoryImpl(entityStorageInitState),
      "testTenantId",
      dOperableCatalog = null,
      fullInference = true)

    new MockedExecutionContext(
      sparkContext,
      sqlContext,
      inferContext,
      fileSystemClient,
      ReportLevel.HIGH,
      "testTenantId")
  }

  protected def assertDataFramesEqual(
      actualDf: DataFrame,
      expectedDf: DataFrame,
      checkRowOrder: Boolean = true): Unit = {
    // Checks only semantic identity, not objects location in memory
    actualDf.sparkDataFrame.schema.treeString shouldBe expectedDf.sparkDataFrame.schema.treeString
    val collectedRows1: Array[Row] = actualDf.sparkDataFrame.collect()
    val collectedRows2: Array[Row] = expectedDf.sparkDataFrame.collect()
    if (checkRowOrder) {
      collectedRows1 shouldBe collectedRows2
    } else {
      collectedRows1 should contain theSameElementsAs collectedRows2
    }
  }

  protected def entityStorageInitState: Map[(String, Entity.Id), Entity] = Map()

  protected def createDataFrame(rows: Seq[Row], schema: StructType): DataFrame = {
    val rdd: RDD[Row] = sparkContext.parallelize(rows)
    val sparkDataFrame = sqlContext.createDataFrame(rdd, schema)
    DataFrameBuilder(sqlContext).buildDataFrame(sparkDataFrame)
  }

  def executeOperation(op: DOperation, dfs: DataFrame*): DataFrame =
    op.execute(executionContext)(dfs.toVector).head.asInstanceOf[DataFrame]

  def createDir(path: String): Unit = {
    new java.io.File(path + "/id").getParentFile.mkdirs()
  }
}

object DeeplangIntegTestSupport {
  val sparkConf: SparkConf = new SparkConf()
    .setMaster("local[4]")
    .setAppName("TestApp")
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .set("spark.cassandra.connection.host", "localhost")
    .set("spark.cassandra.connection.port", "9142")
    .set("spark.cassandra.auth.username", "cassandra")
    .set("spark.cassandra.auth.password", "cassandra")
    .registerKryoClasses(Array())
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  UserDefinedFunctions.registerFunctions(sqlContext.udf)
}

private class MockedExecutionContext(
    override val sparkContext: SparkContext,
    override val sqlContext: SQLContext,
    override val inferContext: InferContext,
    override val fsClient: FileSystemClient,
    override val reportLevel: ReportLevel,
    override val tenantId: String)
  extends ExecutionContext(
    sparkContext,
    sqlContext,
    inferContext,
    fsClient,
    reportLevel,
    tenantId) {

  override def uniqueFsFileName(entityCategory: String): String =
    s"target/tests/$entityCategory/" + UUID.randomUUID().toString
}
