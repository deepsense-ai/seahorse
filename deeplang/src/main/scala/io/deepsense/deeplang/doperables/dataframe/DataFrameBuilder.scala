/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperables.dataframe

import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SQLContext}

/**
 * Deepsense DataFrame builder. Builder performs basic schema validation.
 * @param sqlContext Spark sql context.
 */
class DataFrameBuilder private (sqlContext: SQLContext) extends HasSchemaValidation {

  def buildDataFrame(schema: StructType, data: RDD[Row]): DataFrame = {
    // TODO: validation will be removed. Just for testing purposes.
    validateSchema(schema)
    val dataFrame: sql.DataFrame = sqlContext.createDataFrame(data, schema)
    new DataFrame(Some(dataFrame))
  }

  def buildDataFrame(dataFrame: sql.DataFrame): DataFrame = {
    // TODO: validation will be removed. Just for testing purposes.
    validateSchema(dataFrame.schema)
    new DataFrame(Some(dataFrame))
  }
}

object DataFrameBuilder {
  def apply(sqlContext: SQLContext): DataFrameBuilder = new DataFrameBuilder(sqlContext)
}
