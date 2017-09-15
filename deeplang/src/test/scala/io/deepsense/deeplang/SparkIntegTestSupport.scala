/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}
import org.scalatest.BeforeAndAfterAll

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
}
