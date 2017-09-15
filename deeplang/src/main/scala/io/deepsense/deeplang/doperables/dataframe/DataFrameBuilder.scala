/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SQLContext}

import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMapper

/**
 * DeepSense DataFrame builder. Builder performs basic schema validation.
 * @param sqlContext Spark sql context.
 */
case class DataFrameBuilder(sqlContext: SQLContext) {

  def buildDataFrame(schema: StructType, data: RDD[Row]): DataFrame = {
    val dataFrame: sql.DataFrame = sqlContext.createDataFrame(data, schema)
    DataFrame(dataFrame)
  }

  def buildDataFrame(
      schema: StructType,
      data: RDD[Row],
      categoricalColumns: Seq[String]): DataFrame = {
    val dataFrame: sql.DataFrame = sqlContext.createDataFrame(data, schema)
    CategoricalMapper(buildDataFrame(dataFrame), this).categorized(categoricalColumns: _*)
  }

  def buildDataFrame(sparkDataFrame: sql.DataFrame): DataFrame = {
    DataFrame(sparkDataFrame)
  }

  def buildDataFrame(metadata: DataFrameMetadata, data: RDD[Row]): DataFrame = {
    buildDataFrame(schema = metadata.toSchema, data)
  }
}

object DataFrameBuilder {
  /**
   * @return DataFrame object that can be used _only_ for inference,
   *         i.e. it contains only metadata of this DataFrame.
   */
  def buildDataFrameForInference(metadata: DataFrameMetadata): DataFrame = {
    DataFrame(null, Some(metadata))
  }
}
