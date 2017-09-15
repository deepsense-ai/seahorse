/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor.util

import scala.collection.mutable

import org.scalatest.{FunSuite, Matchers}

class UniqueFilenameUtilSuite extends FunSuite with Matchers {
  val testContainerId = "container_1428054926685_0216_01_000001"

  test("UniqueFilenameUtil should generate directory name in proper format") {
    val hdfsDirectoryName =
      UniqueFilenameUtil.getHdfsDirectoryName("userA", "file", appLocation = "deepsenseB")
    hdfsDirectoryName shouldBe "/deepsenseB/userA/file"
  }

  test("UniqueFilenameUtil should generate filename in proper format") {
    val uniqueHdfsFilename =
      UniqueFilenameUtil.getUniqueHdfsFilename("userA", "file", containerId = testContainerId)
    uniqueHdfsFilename
      .matches("/deepsense/userA/file/application_\\d{13}_\\d{4}_\\d{2}_file\\d{6}") shouldBe true
    uniqueHdfsFilename shouldBe "/deepsense/userA/file/application_1428054926685_0216_01_file000001"
  }

  test("UniqueFilenameUtil should generate unique filenames") {
    val uniqueFilenamesCount = 10000
    val uniqueFilenames = mutable.Set[String]()
    for (x <- Range(0, uniqueFilenamesCount)) {
      uniqueFilenames.add(
        UniqueFilenameUtil.getUniqueHdfsFilename("userA", "file", containerId = testContainerId))
    }
    // NOTE: if some filenames were not unique, set size will be smaller than  uniqueFilenamesCount
    uniqueFilenames.size shouldBe uniqueFilenamesCount
  }
}
