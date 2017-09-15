/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
