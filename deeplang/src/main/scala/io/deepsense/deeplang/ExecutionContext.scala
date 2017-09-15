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

package io.deepsense.deeplang

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.ReportLevel.ReportLevel
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.entitystorage.UniqueFilenameUtil

/** Holds information needed by DOperations and DMethods during execution. */
class ExecutionContext(
    override val dOperableCatalog: DOperableCatalog)
  extends InferContext(dOperableCatalog, fullInference = true) {

  var sparkContext: SparkContext = _
  var sqlContext: SQLContext = _
  var fsClient: FileSystemClient = _

  // Level of details for generated reports
  var reportLevel: ReportLevel = _

  def uniqueFsFileName(entityCategory: String): String =
    UniqueFilenameUtil.getUniqueFsFilename(tenantId, entityCategory)
}
