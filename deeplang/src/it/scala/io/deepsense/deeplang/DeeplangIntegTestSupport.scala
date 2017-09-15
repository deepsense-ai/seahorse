/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.deeplang

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.DFSClient
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll

import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.entitystorage.EntityStorageClientTestInMemoryImpl
import io.deepsense.models.entities.Entity

/**
 * Adds features to facilitate integration testing using Spark and entitystorage
 */
trait DeeplangIntegTestSupport extends UnitSpec with BeforeAndAfterAll {

  val hdfsPath = "hdfs://ds-dev-env-master:8020"

  var executionContext: ExecutionContext = _

  var sparkConf: SparkConf = _
  var sparkContext: SparkContext = _
  var sqlContext: SQLContext = _
  var rawHdfsClient: DFSClient = _

  override def beforeAll(): Unit = {
    sparkConf =
      new SparkConf()
        .setMaster("local[4]")
        .setAppName("TestApp")
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .registerKryoClasses(Array())
    sparkContext = new SparkContext(sparkConf)
    sqlContext = new SQLContext(sparkContext)
    UserDefinedFunctions.registerFunctions(sqlContext.udf)
    rawHdfsClient = new DFSClient(new URI(hdfsPath), new Configuration())

    executionContext = new ExecutionContext
    executionContext.sparkContext = sparkContext
    executionContext.sqlContext = sqlContext
    executionContext.dataFrameBuilder = DataFrameBuilder(sqlContext)
    executionContext.entityStorageClient =
      EntityStorageClientTestInMemoryImpl(entityStorageInitState)
    executionContext.tenantId = "testTenantId"
    executionContext.hdfsClient = new DSHdfsClient(rawHdfsClient)
  }

  override def afterAll(): Unit = sparkContext.stop()

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

  protected def createDataFrame(
      rows: Seq[Row],
      schema: StructType,
      categoricalColumns: Seq[String]): DataFrame = {

    val rdd: RDD[Row] = sparkContext.parallelize(rows)
    DataFrameBuilder(sqlContext).buildDataFrame(schema, rdd, categoricalColumns)
  }

  def executeOperation(op: DOperation, dfs: DataFrame*): DataFrame =
    op.execute(executionContext)(dfs.toVector).head.asInstanceOf[DataFrame]
}
