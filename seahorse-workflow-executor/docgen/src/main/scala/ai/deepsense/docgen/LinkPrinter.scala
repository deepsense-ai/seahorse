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

import java.io.File

import ai.deepsense.deeplang.catalogs.doperations.DOperationCategory

trait LinkPrinter {

  // scalastyle:off println

  def printOperationSiteLinks(
      operationsByCategory: Map[DOperationCategory, Seq[OperationWithSparkClassName]],
      printAll: Boolean): Unit = {
    println("==== Links for operations.md ====")
    printLinksByCategory(
      operationsByCategory,
      (url: String, opName: String) => s"* [$opName]($url)",
      printAll)
  }

  def printOperationMenuLinks(
      operationsByCategory: Map[DOperationCategory, Seq[OperationWithSparkClassName]],
      printAll: Boolean): Unit = {
    println("==== Links for operationsmenu.html ====")
    printLinksByCategory(
      operationsByCategory,
      (url: String, opName: String) => s"""<li><a href="{{base}}/$url">$opName</a></li>""",
      printAll)

  }

  private def printLinksByCategory(
      sparkOperationsByCategory: Map[DOperationCategory, Seq[OperationWithSparkClassName]],
      createLink: (String, String) => String,
      printAll: Boolean): Unit = {

    sparkOperationsByCategory.foreach { case (category, opList) =>
      val linksForCategory = opList.toList.sortBy(_.op.name).flatMap {
        case OperationWithSparkClassName(op, sparkClass) =>
          val underscoredName = DocUtils.underscorize(op.name)
          val url = s"operations/$underscoredName.html"
          val mdFile = new File(s"docs/operations/$underscoredName.md")
          if (!mdFile.exists() || printAll) Some(createLink(url, op.name)) else None
      }
      if(linksForCategory.nonEmpty) {
        println(category.name)
        linksForCategory.foreach(println(_))
        println()
      }
    }
  }
  // scalastyle:on println
}
