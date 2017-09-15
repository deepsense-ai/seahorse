/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.entitystorage

import scala.collection.mutable

import org.scalatest.{FunSuite, Matchers}

class UniqueFilenameUtilSuite extends FunSuite with Matchers {

  test("UniqueFilenameUtil should generate directory name in proper format") {
    val hdfsDirectoryName =
      UniqueFilenameUtil.getHdfsDirectoryName("userA", "file", deploymentDirName = "deepsenseB")
    hdfsDirectoryName shouldBe "/deepsenseB/data/userA/file"
  }

  test("UniqueFilenameUtil should generate filename in proper format") {
    val uniqueHdfsFilename =
      UniqueFilenameUtil.getUniqueHdfsFilename("userA", "file")
    uniqueHdfsFilename.matches(s"/deepsense/data/userA/file/[^_]+_file\\d{6}") shouldBe true
  }

  test("UniqueFilenameUtil should generate filename in temporary directory") {
    val uniqueHdfsFilename =
      UniqueFilenameUtil.getUniqueHdfsFilename("userTmp", "file", isTemporary = true)
    uniqueHdfsFilename.matches(s"/deepsense/tmp/userTmp/file/[^_]+_file\\d{6}") shouldBe true
  }

  test("UniqueFilenameUtil should generate unique filenames") {
    val uniqueFilenamesCount = 10000
    val uniqueFilenames = mutable.Set[String]()
    for (x <- Range(0, uniqueFilenamesCount)) {
      uniqueFilenames.add(
        UniqueFilenameUtil.getUniqueHdfsFilename("userA", "file"))
    }
    // NOTE: if some filenames were not unique, set size will be smaller than  uniqueFilenamesCount
    uniqueFilenames.size shouldBe uniqueFilenamesCount
  }
}
