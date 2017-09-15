/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.dataframe.{DataFrameBuilder, DataFrame}
import io.deepsense.deeplang.{SparkIntegTestSupport, DOperable, ExecutionContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row

class ReadDataFrameIntegSpec extends SparkIntegTestSupport {
  "ReadDataFrame" should "read locally saved DataFrame" in {
    val localDataFramePath = "src/test/resources/localDataFrameSample"

    // read DataFrame using operation
    val context = new ExecutionContext
    context.sqlContext = sqlContext
    context.dataFrameBuilder = DataFrameBuilder(sqlContext)

    val operation = new ReadDataFrame
    val pathParameter = operation.parameters.getStringParameter("path")
    pathParameter.value = Some(localDataFramePath)

    val operationResult = operation.execute(context)(Vector.empty[DOperable])
    val operationDataFrame = operationResult(0).asInstanceOf[DataFrame]

    // compare DataFrames
    val schema: StructType = StructType(List(
      StructField("column1", StringType, true),
      StructField("column2", LongType, true),
      StructField("column3", DoubleType, true),
      StructField("column4", StringType, true)))

    val manualRowsSeq: Seq[Row] = Seq(
      Row("aaa", 1L, 1.2, "2007-12-02 03:10:11.0000001"),
      Row("bbb", 2L, 2.2, "2007-12-02 03:10:11.0000001"),
      Row("ccc", 3L, 3.4, "2007-12-02 03:10:11.0000001"))

    val manualRDD: RDD[Row] = sparkContext.parallelize(manualRowsSeq)

    val manualRows = sqlContext.createDataFrame(manualRDD, schema).collect()
    val operationRows = operationDataFrame.sparkDataFrame.collect()

    assert(operationRows.length == manualRows.length)

    val zipped = operationRows zip manualRows
    assert(zipped.forall(rowPair => rowPair._1 == rowPair._2))
    assert(zipped.forall(rowPair => rowPair._1.schema == rowPair._2.schema))
  }
}
