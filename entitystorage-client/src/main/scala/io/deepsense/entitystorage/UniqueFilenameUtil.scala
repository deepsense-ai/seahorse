/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.entitystorage

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Utility for creating unique filenames on HDFS.
 */
object UniqueFilenameUtil {
  val DataFrameEntityCategory = "dataframe"

  private val numberGenerator = new AtomicInteger(0)

  /** String unique for UniqueFilenameUtil instance. It is used to create unique file path */
  val uniqueString = UUID.randomUUID()

  /**
   * Returns directory for storing files of given properties.
   * NOTE: User can create that directory using single command:
   * cli.get.mkdirs(directoryName, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL), true)
   * @param tenantId tenant id
   * @param entityCategory category of entity (file/dataframe/etc..)
   * @param appLocation optional: HDFS location of deepsense application
   * @return directory name for storing files of given properties
   */
  def getHdfsDirectoryName(
      tenantId: String,
      entityCategory: String,
      appLocation: String = "deepsense"): String = {
    s"/$appLocation/$tenantId/$entityCategory"
  }

  /**
   * Returns unique HDFS filename for file of given properties.
   * @param tenantId tenant id
   * @param entityCategory category of entity (file/dataframe/etc..)
   * @param appLocation optional: HDFS location of deepsense application
   * @return unique HDFS filename for file of given properties
   */
  def getUniqueHdfsFilename(
      tenantId: String,
      entityCategory: String,
      appLocation: String = "deepsense"): String = {
    val directoryName = getHdfsDirectoryName(tenantId, entityCategory, appLocation)
    val uniqueNumberStr = "%06d".format(numberGenerator.incrementAndGet())
    s"$directoryName/${uniqueString}_file$uniqueNumberStr"
  }
}
