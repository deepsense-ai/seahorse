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

package ai.deepsense.deeplang.documentation

import ai.deepsense.deeplang.DOperation

trait SparkOperationDocumentation extends OperationDocumentation { self: DOperation =>
  private val sparkVersion = org.apache.spark.SPARK_VERSION
  private val sparkDocsUrl = s"https://spark.apache.org/docs/$sparkVersion/"
  protected[this] val docsGuideLocation: Option[String]

  /**
   * Generates Spark's guide section with a link. Used by docgen.
   */
  override def generateDocs: Option[String] = {
    docsGuideLocation.map(
      guideLocation => {
        val url = sparkDocsUrl + guideLocation
        s"""|For a comprehensive introduction, see
            |<a target="_blank" href="$url">Spark documentation</a>.""".stripMargin
      }
    )
  }
}

