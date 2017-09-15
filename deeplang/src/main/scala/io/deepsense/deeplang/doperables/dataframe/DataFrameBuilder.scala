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

/**
 * DeepSense DataFrame builder.
 * @param sqlContext Spark sql context.
 */
case class DataFrameBuilder(sqlContext: SQLContext) {

  def buildDataFrame(schema: StructType, data: RDD[Row]): DataFrame = {
    val dataFrame: sql.DataFrame = sqlContext.createDataFrame(data, schema)
    buildDataFrame(dataFrame)
  }

  def buildDataFrame(sparkDataFrame: sql.DataFrame): DataFrame = {
    DataFrame(sparkDataFrame, Some(sparkDataFrame.schema))
  }
}

object DataFrameBuilder
