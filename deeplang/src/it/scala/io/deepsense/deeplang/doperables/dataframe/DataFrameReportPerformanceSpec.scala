/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import java.sql.Timestamp
import java.text.{DateFormat, SimpleDateFormat}
import java.util.TimeZone

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType, TimestampType}
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.utils.{DoubleUtils, Logging}
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperations.DOperationsFactory
@Ignore
//It's ignored because it doesn't have got assertions, it only prints report generation time.
class DataFrameReportPerformanceSpec
    extends DeeplangIntegTestSupport
    with BeforeAndAfter
    with Logging
    with DOperationsFactory {
  val testFile = "/tests/demand.csv"

  before {
    rawHdfsClient.delete(testFile, true)
    executionContext.hdfsClient.copyLocalFile(
      this.getClass.getResource("/csv/demand_without_header.csv").getPath,
      testFile)
  }

  after {
    rawHdfsClient.delete(testFile, true)
  }

  "DataFrame" should {
    "generate report" when {
      "DataFrame has 17K of rows" in {
        val numberOfTries = 10
        var results: Seq[Double] = Seq()
        for (i <- 1 to numberOfTries) {
          val dataFrame: DataFrame = demandDataFrame()
          val start = System.nanoTime()
          val report = dataFrame.report
          val end = System.nanoTime()
          val time1: Double = (end - start).toDouble / 1000000000.0
          results = results :+ time1
          logger.info("Report generation time: {}", DoubleUtils.double2String(time1))
        }
        logger.info(
          "Mean report generation time: {}",
          DoubleUtils.double2String(results.fold(0D)(_ + _) / numberOfTries.toDouble))
      }
    }
  }

  private def demandDataFrame(): DataFrame = {
    val rddString: RDD[String] = executionContext.sparkContext.textFile(testFile)
    val data: RDD[Row] = rddString.map(DataFrameHelpers.demandString2Row)
    executionContext.dataFrameBuilder.buildDataFrame(demandSchema, data)
  }

  private def demandSchema: StructType = StructType(Seq(
    StructField("datetime", TimestampType),
    StructField("log_count", DoubleType),
    StructField("workingday", DoubleType),
    StructField("holiday", DoubleType),
    StructField("season2", DoubleType),
    StructField("season3", DoubleType),
    StructField("season4", DoubleType)))

  private def timestamp(s: String): Timestamp = {
    val format: DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    format.setTimeZone(TimeZone.getTimeZone("UTC"))
    new Timestamp(format.parse(s).getTime)
  }
}

private object DataFrameHelpers {
  def demandString2Row(s: String): Row = {
    val split = s.split(",")
    Row(
      timestamp(split(0)),
      split(1).toDouble,
      split(2).toDouble,
      split(3).toDouble,
      split(4).toDouble,
      split(5).toDouble,
      split(6).toDouble
    )
  }

  private def timestamp(s: String): Timestamp = {
    val format: DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    format.setTimeZone(TimeZone.getTimeZone("UTC"))
    new Timestamp(format.parse(s).getTime)
  }
}
