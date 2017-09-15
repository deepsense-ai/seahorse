/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang

import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll

import io.deepsense.deeplang.dataframe.{DataFrame, DataFrameBuilder}

/**
 * Adds features to aid integration testing using spark
 */
trait SparkIntegTestSupport extends UnitSpec with BeforeAndAfterAll {
  var sparkConf: SparkConf = _
  var sparkContext: SparkContext = _
  var sqlContext: SQLContext = _

  override def beforeAll: Unit = {
    sparkConf = new SparkConf().setMaster("local[4]").setAppName("TestApp")
    sparkContext = new SparkContext(sparkConf)
    sqlContext = new SQLContext(sparkContext)
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
}
