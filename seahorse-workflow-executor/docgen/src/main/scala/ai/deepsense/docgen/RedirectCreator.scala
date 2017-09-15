/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.docgen

import java.io.{File, PrintWriter}

import ai.deepsense.deeplang.DOperation

trait RedirectCreator {

  // scalastyle:off println

  /**
    * @return number of redirects created
    */
  def createRedirects(
      sparkOperations: Seq[OperationWithSparkClassName],
      forceUpdate: Boolean): Int = {

    sparkOperations.map { case OperationWithSparkClassName(operation, sparkClassName) =>
      val redirectFile = new File("docs/uuid/" + operation.id + ".md")
      if (!redirectFile.exists() || forceUpdate) {
        createRedirect(redirectFile, operation, sparkClassName)
        1
      } else {
        0
      }
    }.sum
  }

  private def createRedirect(redirectFile: File, operation: DOperation, sparkClassName: String) = {
    val writer = new PrintWriter(redirectFile)
    writer.println("---")
    writer.println("layout: redirect")
    writer.println("redirect: ../operations/" + DocUtils.underscorize(operation.name) + ".html")
    writer.println("---")
    writer.flush()
    writer.close()
    println("Created redirect for " + operation.name)
  }
  // scalastyle:on println
}
