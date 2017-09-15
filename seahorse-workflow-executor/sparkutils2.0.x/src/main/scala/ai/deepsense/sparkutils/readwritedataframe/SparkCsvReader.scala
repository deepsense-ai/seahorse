/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.execution.LogicalRDD
import org.apache.spark.sql.types.{StructType, _}

object SparkCsvReader {
  def create(options: CSVOptions): LineCsvReader = new LineCsvReader(options)
  def univocityTokenizer(
      rdd: RDD[String],
      header: Array[String],
      firstLine: String,
      options: CSVOptions): RDD[Array[String]] =
    CSVRelation.univocityTokenizer(rdd, header, firstLine, options)
}
