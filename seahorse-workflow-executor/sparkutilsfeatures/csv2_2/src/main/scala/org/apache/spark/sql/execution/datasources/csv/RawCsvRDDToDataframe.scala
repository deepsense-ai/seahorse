/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import scala.util.Try

import com.univocity.parsers.csv.CsvParser
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.execution.LogicalRDD
import org.apache.spark.sql.types.{StructType, _}


/**
  * Heavily based on org.apache.spark.sql.execution.datasources.csv.CSVFileFormat
  */
object RawCsvRDDToDataframe {

  def parse(
      rdd: RDD[String],
      sparkSession: SparkSession,
      options: Map[String, String]): DataFrame = {
    val csvOptions = new CSVOptions(options, sparkSession.sessionState.conf.sessionLocalTimeZone)
    val csvReader = new CsvParser(csvOptions.asParserSettings)
    val firstLine = findFirstLine(csvOptions, rdd)
    val firstRow = csvReader.parseLine(firstLine)
    val header = if (csvOptions.headerFlag) {
      firstRow.zipWithIndex.map { case (value, index) =>
        if (value == null || value.isEmpty || value == csvOptions.nullValue) s"_c$index" else value
      }
    } else {
      firstRow.zipWithIndex.map { case (value, index) => s"_c$index" }
    }

    // TODO Migrate to Spark's schema inferencer eventually
    // val schema = CSVInferSchema.infer(parsedRdd, header, csvOptions)
    val schema = {
      val schemaFields = header.map { fieldName =>
        StructField(fieldName.toString, StringType, nullable = true)
      }
      StructType(schemaFields)
    }

    val withoutHeader = if (csvOptions.headerFlag) {
      rdd.zipWithIndex()
        .filter { case (row, index) => index != 0 }
        .map { case (row, index) => row }
    }
    else {
      rdd
    }


    val internalRows = withoutHeader.filter(row => row.trim.nonEmpty)
      .flatMap { row =>
      val univocityParser = new UnivocityParser(schema, csvOptions)
      Try(univocityParser.parse(row)).toOption
    }

    Dataset.ofRows(
      sparkSession,
      LogicalRDD(
        schema.toAttributes,
        internalRows)(sparkSession))
  }

  private def findFirstLine(options: CSVOptions, rdd: RDD[String]): String = {
    if (options.isCommentSet) {
      val comment = options.comment.toString
      rdd.filter { line =>
        line.trim.nonEmpty && !line.startsWith(comment)
      }.first()
    } else {
      rdd.filter { line =>
        line.trim.nonEmpty
      }.first()
    }
  }

}
