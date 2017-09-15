/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.dataframe

import java.util.UUID

import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{Row, SQLContext}

/**
 * Deepsense DataFrame builder. Builder performs basic schema validation.
 * @param sqlContext Spark sql context.
 */
class DataFrameBuilder(sqlContext: SQLContext) extends HasSchemaValidation {

  def buildDataFrame(id: UUID, schema: StructType, data: RDD[Row]): DataFrame = {
    validateSchema(schema)
    val dataFrame: sql.DataFrame = sqlContext.createDataFrame(data, schema)
    new DataFrame(id, dataFrame)
  }

}
