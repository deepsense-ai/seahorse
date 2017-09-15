/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import scala.concurrent.Await

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperationIntegTestSupport, ExecutionContext, DOperable}
import io.deepsense.models.entities.{Entity, DataObjectReference, DataObjectReport, InputEntity}

class ReadDataFrameIntegSpec extends DOperationIntegTestSupport with BeforeAndAfter with LazyLogging {

  val timestamp: Timestamp = new Timestamp(new DateTime(2007, 12, 2, 3, 10, 11).getMillis)

  val testDir = "/tests/ReadDataFrameTest"

  before {
    hdfsClient.delete(testDir, true)
  }

  "ReadDataFrame" should "read saved DataFrame" in {
    val context = executionContext
    val dataFrame: DataFrame = testDataFrame(context.dataFrameBuilder)
    dataFrame.sparkDataFrame.saveAsParquetFile(testDir)

    val entity = registerDataFrame(context)

    val operation = new ReadDataFrame
    val idParameter = operation.parameters.getStringParameter(ReadDataFrame.idParam)
    idParameter.value = Some(entity.id.toString)
    logger.info("Reading dataframe from entity id: {}", idParameter)
    val operationResult = operation.execute(context)(Vector.empty[DOperable])
    val operationDataFrame = operationResult(0).asInstanceOf[DataFrame]

    assertDataFramesEqual(dataFrame, operationDataFrame)
  }

  def registerDataFrame(context: ExecutionContext): Entity = {
    import scala.concurrent.duration._
    implicit val timeout = 5.seconds
    val entityF = context.entityStorageClient.createEntity(InputEntity(
      context.tenantId,
      "testEntity name",
      "testEntity description",
      "DataFrame",
    // TODO: check if ds-dev-env-master:8020 is necessary, if yes - get it from configuration
      Some(DataObjectReference(s"$hdfsPath$testDir")),
      Some(DataObjectReport("testEntity Report")),
      true))
    Await.result(entityF, timeout)
  }

  def testDataFrame(builder: DataFrameBuilder): DataFrame = {
    val schema: StructType = StructType(List(
      StructField("column1", StringType, true),
      StructField("column2", LongType, true),
      StructField("column3", DoubleType, true),
      StructField("column4", TimestampType, true)))
    val manualRowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, timestamp),
      Row("bbb", 2L, 2.2, timestamp),
      Row("ccc", 3L, 3.4, timestamp))
    val manualRDD: RDD[Row] = sparkContext.parallelize(manualRowsSeq)
    builder.buildDataFrame(schema, manualRDD)
  }
}
