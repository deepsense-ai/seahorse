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

import java.io.PrintWriter
import com.univocity.parsers.csv.{CsvWriter, CsvWriterSettings}
import org.apache.spark.sql.types._

/**
  * Heavily based on org.apache.spark.sql.execution.datasources.csv.CsvOutputWriter
  * Instead of writing to Hadoop Text File it writes to local file system
  */
class LocalCsvOutputWriter(
      schema: StructType,
      options: CSVOptions,
      driverPath: String) {

  private val driverFileWriter = new PrintWriter(driverPath)

  private val FLUSH_BATCH_SIZE = 1024L
  private var records: Long = 0L
  private val writerSettings = createWriterSettings(schema, options)
  private val gen = new CsvWriter(driverFileWriter, writerSettings)

  def write(row: Seq[String]): Unit = {
    gen.writeRow(row.toArray)
    records += 1
    if (records % FLUSH_BATCH_SIZE == 0) {
      flush()
    }
  }

  def close(): Unit = {
    flush()
    driverFileWriter.close()
  }

  private def flush(): Unit = {
    gen.flush()
  }

  private def createWriterSettings(schema: StructType, options: CSVOptions): CsvWriterSettings = {
    val writerSettings = options.asWriterSettings
    writerSettings.setHeaderWritingEnabled(options.headerFlag)
    writerSettings.setHeaders(schema.fieldNames: _*)
    writerSettings
  }
}
