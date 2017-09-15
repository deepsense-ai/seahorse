/**
 * Copyright 2016, deepsense.ai
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

package org.apache.spark.sql.execution.datasources.csv

import com.databricks.spark.csv.{CsvRelation, DeepsenseDefaultSource}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._

import io.deepsense.sparkutils.SparkSQLSession

object RawCsvRDDToDataframe {

  def parse(
      rdd: RDD[String],
      sparkSQLSession: SparkSQLSession,
      options: Map[String, String]): DataFrame = {

    val sqlContext = sparkSQLSession.getSQLContext
    val relation = DeepsenseDefaultSource.createRelation(sqlContext, options, rdd
    ).asInstanceOf[CsvRelation]
    sqlContext.baseRelationToDataFrame(relation)
  }
}
