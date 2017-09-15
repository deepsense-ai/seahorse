/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.entitystorage

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Utility for creating unique filenames on HDFS.
 */
object UniqueFilenameUtil {
  val DataFrameEntityCategory = "dataframe"
  val ModelEntityCategory = "model"
  val FileEntityCategory = "file"

  private val numberGenerator = new AtomicInteger(0)

  /** String unique for UniqueFilenameUtil instance. It is used to create unique file path */
  val uniqueString = UUID.randomUUID()

  /**
   * Returns directory for storing files of given properties.
   * NOTE: User can create that directory using single command:
   * cli.get.mkdirs(directoryName, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL), true)
   * @param tenantId tenant id
   * @param entityCategory category of entity (file/dataframe/etc..)
   * @param deploymentDirName optional: HDFS location of deepsense application
   * @return directory name for storing files of given properties
   */
  def getHdfsDirectoryName(
      tenantId: String,
      entityCategory: String,
      deploymentDirName: String = "deepsense",
      isTemporary: Boolean = false): String = {
    val subDirectory = if (isTemporary) "tmp" else "data"
    s"/$deploymentDirName/$subDirectory/$tenantId/$entityCategory"
  }

  /**
   * Returns unique HDFS filename for file of given properties.
   * @param tenantId tenant id
   * @param entityCategory category of entity (file/dataframe/etc..)
   * @param deploymentDirName optional: HDFS location of deepsense application
   * @return unique HDFS filename for file of given properties
   */
  def getUniqueHdfsFilename(
      tenantId: String,
      entityCategory: String,
      deploymentDirName: String = "deepsense",
      isTemporary: Boolean = false): String = {
    val directoryName =
      getHdfsDirectoryName(tenantId, entityCategory, deploymentDirName, isTemporary)
    val uniqueNumberStr = "%06d".format(numberGenerator.incrementAndGet())
    s"$directoryName/${uniqueString}_file$uniqueNumberStr"
  }
}
