/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.{BeforeAndAfter, Ignore}

import io.deepsense.deeplang.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.{DOperationIntegTestSupport, DOperable, ExecutionContext}

/**
 * This test requirements:
 * - deepsense dev environment
 * - /tests directory with read,write privileges to Your user on HDFS
 * - /etc/hosts entry: 172.28.128.100 ds-dev-env-master
 */
// TODO: shouldn't it be WriteReadDataFrameIntegSpec ?
// NOTE: ignored because of impossibility to pass id of written DF to readDF operation
@Ignore
class ReadWriteDataFrameIntegSpec extends DOperationIntegTestSupport with BeforeAndAfter {

  val testDir = "/tests/readWriteDataFrameTest"

  before {
    hdfsClient.delete(testDir, true)
  }

  "Write and Read DataFrame operations" should "correctly write and read dataFrame from HDFS" in {
    val context = executionContext
    val dataFrame: DataFrame = createDataFrame

    writeDataFrame(context, dataFrame, "test name", "test description")
    // TODO: is it possible to get entity id by its name & description?
    val entityId = "???"
    val retrievedDataFrame = readDataFrame(context, entityId)

    assertDataFramesEqual(dataFrame, retrievedDataFrame)
  }

  private def createDataFrame: DataFrame = {
    val rowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, null),
      Row("bbb", 2L, null, "text"),
      Row("ccc", null, 3.4, "when the music is over turn off the lights.")
    )
    val schema: StructType = StructType(List(
      StructField("column1", StringType, nullable = true),
      StructField("column2", LongType, nullable = true),
      StructField("column3", DoubleType, nullable = true),
      StructField("column4", StringType, nullable = true))
    )
    val manualRDD: RDD[Row] = sparkContext.parallelize(rowsSeq)
    val sparkDataFrame = sqlContext.createDataFrame(manualRDD, schema)
    val builder = DataFrameBuilder(sqlContext)
    builder.buildDataFrame(sparkDataFrame)
  }

  private def writeDataFrame(
      context: ExecutionContext,
      dataFrame: DataFrame,
      name: String,
      description: String): Unit = {
    val writeDataFrameOperation = new WriteDataFrame
    val nameParameter =
      writeDataFrameOperation.parameters.getStringParameter(WriteDataFrame.nameParam)
    nameParameter.value = Some(name)
    val descriptionParameter =
      writeDataFrameOperation.parameters.getStringParameter(WriteDataFrame.descriptionParam)
    descriptionParameter.value = Some(description)

    writeDataFrameOperation.execute(context)(Vector[DOperable](dataFrame))
  }

  private def readDataFrame(context: ExecutionContext, entityId: String): DataFrame = {
    val readDataFrameOperation = new ReadDataFrame
    val idParameter = readDataFrameOperation.parameters.getStringParameter(ReadDataFrame.idParam)
    idParameter.value = Some(entityId)

    val operationResult = readDataFrameOperation.execute(context)(Vector.empty[DOperable])
    operationResult.head.asInstanceOf[DataFrame]
  }
}
