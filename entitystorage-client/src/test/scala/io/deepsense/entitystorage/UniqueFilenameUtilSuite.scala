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

import scala.collection.mutable

import org.scalatest.{FunSuite, Matchers}

class UniqueFilenameUtilSuite extends FunSuite with Matchers {

  test("UniqueFilenameUtil should generate directory name in proper format") {
    val fsDirectoryName =
      UniqueFilenameUtil.getFsDirectoryName("userA", "file", deploymentDirName = "deepsenseB")
    fsDirectoryName shouldBe "/deepsenseB/data/userA/file"
  }

  test("UniqueFilenameUtil should generate filename in proper format") {
    val uniqueFsFilename =
      UniqueFilenameUtil.getUniqueFsFilename("userA", "file")
    uniqueFsFilename.matches(s"/deepsense/data/userA/file/[^_]+_file\\d{6}") shouldBe true
  }

  test("UniqueFilenameUtil should generate filename in temporary directory") {
    val uniqueFsFilename =
      UniqueFilenameUtil.getUniqueFsFilename("userTmp", "file", isTemporary = true)
    uniqueFsFilename.matches(s"/deepsense/tmp/userTmp/file/[^_]+_file\\d{6}") shouldBe true
  }

  test("UniqueFilenameUtil should generate unique filenames") {
    val uniqueFilenamesCount = 10000
    val uniqueFilenames = mutable.Set[String]()
    for (x <- Range(0, uniqueFilenamesCount)) {
      uniqueFilenames.add(
        UniqueFilenameUtil.getUniqueFsFilename("userA", "file"))
    }
    // NOTE: if some filenames were not unique, set size will be smaller than  uniqueFilenamesCount
    uniqueFilenames.size shouldBe uniqueFilenamesCount
  }
}
