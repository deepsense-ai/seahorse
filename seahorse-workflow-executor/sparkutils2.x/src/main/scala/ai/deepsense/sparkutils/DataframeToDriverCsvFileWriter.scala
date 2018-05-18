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

import ai.deepsense.sparkutils.readwritedataframe.ManagedResource
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

object DataframeToDriverCsvFileWriter {

  def write(
    dataFrame: DataFrame,
    options: Map[String, String],
    dataSchema: StructType,
    pathWithoutScheme: String,
    sparkSession: SparkSession): Unit = {
    val data = dataFrame.rdd.collect()
    val params = MapToCsvOptions(options, sparkSession.sessionState.conf)
    ManagedResource(
      new LocalCsvOutputWriter(dataSchema, params, pathWithoutScheme)
    ) { writer =>
      data.foreach(row => {
        writer.write(row.toSeq.map(_.asInstanceOf[String]))
      })
    }
  }

}
