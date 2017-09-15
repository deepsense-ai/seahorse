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

import java.io.PrintWriter

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types._

import ai.deepsense.sparkutils.readwritedataframe.{DataframeToRawCsvRDD, ManagedResource}

object DataframeToDriverCsvFileWriter {

  def write(
      dataFrame: DataFrame,
      options: Map[String, String],
      dataSchema: StructType,
      pathWithoutScheme: String): Unit = {
    val rawCsvLines = DataframeToRawCsvRDD(dataFrame, options)(dataFrame.sqlContext.sparkContext)
    writeRddToDriverFile(pathWithoutScheme, rawCsvLines)
  }

  // TODO extract to commons from DriverFiles
  private def writeRddToDriverFile(driverPath: String, lines: RDD[String]): Unit = {
    val recordSeparator = System.getProperty("line.separator", "\n")
    ManagedResource(new PrintWriter(driverPath)) { writer =>
      lines.collect().foreach(line => writer.write(line + recordSeparator))
    }
  }

}
