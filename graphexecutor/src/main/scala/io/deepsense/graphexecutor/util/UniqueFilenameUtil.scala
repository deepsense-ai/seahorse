/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor.util

import java.util.concurrent.atomic.AtomicInteger

/**
 * Utility for creating unique filenames on HDFS.
 */
object UniqueFilenameUtil {
  private val numberGenerator = new AtomicInteger(0)

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
   * @param containerId optional: current container id
   * @param appLocation optional: HDFS location of deepsense application
   * @return unique HDFS filename for file of given properties
   */
  def getUniqueHdfsFilename(
      tenantId: String,
      entityCategory: String,
      containerId: String = System.getenv("CONTAINER_ID"),
      appLocation: String = "deepsense"): String = {
    require(containerId != null)
    require(containerId.matches("container_\\d{13}_\\d{4}_\\d{2}_\\d{6}"))
    val directoryName = getHdfsDirectoryName(tenantId, entityCategory, appLocation)
    val uniqueAppAttemptId =
      containerId.substring(0, containerId.lastIndexOf("_")).replace("container", "application")
    val uniqueNumberStr = "%06d".format(numberGenerator.incrementAndGet())
    s"$directoryName/${uniqueAppAttemptId}_file$uniqueNumberStr"
  }
}
