/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
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

/**
 * Adds features to aid integration testing using spark
 */
trait SparkIntegTestSupport extends UnitSpec with BeforeAndAfterAll {

  val hdfsPath = "hdfs://ds-dev-env-master:8020"  // TODO extract in to configuration file
  var sparkConf: SparkConf = _
  var sparkContext: SparkContext = _
  var sqlContext: SQLContext = _
  var hdfsClient: DFSClient = _

  override def beforeAll: Unit = {
    sparkConf = new SparkConf().setMaster("local[4]").setAppName("TestApp")
    sparkContext = new SparkContext(sparkConf)
    sqlContext = new SQLContext(sparkContext)
    hdfsClient = new DFSClient(new URI(hdfsPath), new Configuration())
  }

  override def afterAll: Unit = {
    sparkContext.stop()
  }

  protected def executionContext: ExecutionContext = {
    val context = new ExecutionContext
    context.sqlContext = sqlContext
    context.dataFrameBuilder = DataFrameBuilder(sqlContext)
    context
  }

  protected def assertDataFramesEqual(dt1: DataFrame, dt2: DataFrame): Unit = {
    assert(dt1.sparkDataFrame.schema == dt2.sparkDataFrame.schema)
    val collectedRows1: Array[Row] = dt1.sparkDataFrame.collect()
    val collectedRows2: Array[Row] = dt2.sparkDataFrame.collect()
    collectedRows1 should be (collectedRows2)
  }

  protected def createDataFrame(rowsSeq: Seq[Row], schema: StructType): DataFrame = {
    val manualRDD: RDD[Row] = sparkContext.parallelize(rowsSeq)
    val sparkDataFrame = sqlContext.createDataFrame(manualRDD, schema)
    val builder = DataFrameBuilder(sqlContext)
    builder.buildDataFrame(sparkDataFrame)
  }
}
