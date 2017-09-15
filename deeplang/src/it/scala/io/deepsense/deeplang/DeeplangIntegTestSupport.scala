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

  override def beforeAll: Unit = {
    sparkConf = new SparkConf().setMaster("local[4]").setAppName("TestApp")
    sparkContext = new SparkContext(sparkConf)
    sqlContext = new SQLContext(sparkContext)
    rawHdfsClient = new DFSClient(new URI(hdfsPath), new Configuration())

    executionContext = new ExecutionContext
    executionContext.sqlContext = sqlContext
    executionContext.dataFrameBuilder = DataFrameBuilder(sqlContext)
    executionContext.entityStorageClient =
      EntityStorageClientTestInMemoryImpl(entityStorageInitState)
    executionContext.tenantId = "testTenantId"
    executionContext.hdfsClient = new DSHdfsClient(rawHdfsClient)
  }

  override def afterAll: Unit = {
    sparkContext.stop()
  }

  protected def assertDataFramesEqual(dt1: DataFrame, dt2: DataFrame): Unit = {
    assert(dt1.sparkDataFrame.schema == dt2.sparkDataFrame.schema)
    val collectedRows1: Array[Row] = dt1.sparkDataFrame.collect()
    val collectedRows2: Array[Row] = dt2.sparkDataFrame.collect()
    collectedRows1 should be (collectedRows2)
  }

  protected def entityStorageInitState: Map[(String, Entity.Id), Entity] = Map()

  protected def createDataFrame(rowsSeq: Seq[Row], schema: StructType): DataFrame = {
    val manualRDD: RDD[Row] = sparkContext.parallelize(rowsSeq)
    val sparkDataFrame = sqlContext.createDataFrame(manualRDD, schema)
    val builder = DataFrameBuilder(sqlContext)
    builder.buildDataFrame(sparkDataFrame)
  }
}
