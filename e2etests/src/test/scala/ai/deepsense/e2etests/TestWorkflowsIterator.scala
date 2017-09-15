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

package ai.deepsense.e2etests

import java.io.File
import java.net.URI

object TestWorkflowsIterator {
  private val testsDirUri = getClass.getResource(s"/workflows").toURI
  private val testsDir = new File(testsDirUri.getPath)

  case class Input(workflowLocation: URI, fileContents: String)

  def foreach(f: Input => Unit): Unit = foreachInDirectory(f, testsDir)

  private def foreachInDirectory(f: Input => Unit, dir: File): Unit = {
    dir.listFiles.filter(!_.getName.startsWith("IGNORED")).foreach { file =>
      if (file.isFile) {
        val relativePath = testsDirUri.relativize(file.toURI)
        val source = scala.io.Source.fromFile(file)
        val contents = try source.getLines().mkString("\n") finally source.close()
        f(Input(relativePath, contents))
      } else {
        foreachInDirectory(f, file)
      }
    }
  }
}
