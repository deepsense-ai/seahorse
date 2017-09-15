/**
 * Copyright 2016, deepsense.io
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

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

trait TestFiles { self: BeforeAndAfter with BeforeAndAfterAll =>

  private val fileSystemClient = LocalFileSystemClient()

  before {
    fileSystemClient.delete(testsDir)
    new java.io.File(testsDir + "/id").getParentFile.mkdirs()
    fileSystemClient.copyLocalFile(getClass.getResource("/test_files/").getPath, testsDir)
  }

  after {
    fileSystemClient.delete(testsDir)
  }

  private val testsDir = "target/tests"
  val absoluteTestsDirPath = new java.io.File(testsDir).getAbsoluteFile.toString

}
