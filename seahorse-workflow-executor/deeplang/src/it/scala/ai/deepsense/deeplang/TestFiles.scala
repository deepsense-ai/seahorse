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

package ai.deepsense.deeplang

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

import ai.deepsense.deeplang.doperations.inout.InputFileFormatChoice
import ai.deepsense.deeplang.doperations.readwritedataframe.{FilePath, FileScheme}

trait TestFiles { self: BeforeAndAfter with BeforeAndAfterAll =>

  private def fileSystemClient = LocalFileSystemClient()

  before {
    fileSystemClient.delete(testsDir)
    new java.io.File(testsDir + "/id").getParentFile.mkdirs()
    fileSystemClient.copyLocalFile(getClass.getResource("/test_files/").getPath, testsDir)
  }

  after {
    fileSystemClient.delete(testsDir)
  }

  def testFile(fileFormat: InputFileFormatChoice, fileScheme: FileScheme): String = {
    val format = fileFormat.getClass.getSimpleName.toLowerCase()
    val fileName = s"some_$format.$format"
    val path = fileScheme match {
      case FileScheme.HTTPS => "https://s3.amazonaws.com/workflowexecutor/test_data/"
      case FileScheme.File => absoluteTestsDirPath.fullPath
      case other => throw new IllegalStateException(s"$other not supported")
    }
    val fullPath = path + fileName
    fullPath
  }

  def someCsvFile = FilePath(FileScheme.File, testsDir + "/some_csv.csv")

  private val testsDir = "target/tests"

  def absoluteTestsDirPath: FilePath = FilePath(FileScheme.File, rawAbsoluteTestsDirPath)
  private def rawAbsoluteTestsDirPath = new java.io.File(testsDir).getAbsoluteFile.toString + "/"

}
