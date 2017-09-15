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

import java.util.Scanner

/**
 * This app generates Seahorse documentation for Spark ported operations.
 * It is capable of generating redirects and documentation pages automatically.
 * To achieve that, it uses CatalogRecorder and reflection.
 *
 * Link generation for operation menu and operation subpage is semi-automatic. The app
 * prints links to System.out and the user is responsible for inserting them
 * into proper places in operations.md and operationsmenu.html files.
 *
 * The typical use case consists of:
 *   1. Running the app in link generation mode for new operations.
 *   2. Manually inserting generated links to operations.md and operationsmenu.html files.
 *   3. Running the app in documentation page & redirect creation mode (for new operations).
 *
 * Order of operations is important, because link generation recognizes new operations
 * by non-existence of their documentation pages.
 */
object SparkOperationsDocGenerator
  extends DocPageCreator
  with SparkOperationsExtractor
  with RedirectCreator
  with LinkPrinter {

  val sparkVersion = org.apache.spark.SPARK_VERSION
  val scalaDocPrefix = s"https://spark.apache.org/docs/$sparkVersion/api/scala/index.html#"

  // scalastyle:off println
  def main(args: Array[String]): Unit = {

    val sc = new Scanner(System.in)

    println("==========================")
    println("= Seahorse doc generator =")
    println("==========================")
    println
    println("What do you want to do?")
    println("[P]rint links to new Spark operations")
    println("[C]reate documentation pages and redirects for Spark operations")
    print("> ")

    sc.nextLine().toLowerCase match {
      case "p" =>
        println("Do you want to print [A]ll links or only links to [N]ew operations?")
        print("> ")
        sc.nextLine().toLowerCase match {
          case "a" => printLinks(true)
          case "n" => printLinks(false)
          case _ => wrongInputExit()
        }
      case "c" =>
        println("Do you want to [R]ecreate all pages and redirects or [U]pdate for new operations?")
        print("> ")
        sc.nextLine().toLowerCase match {
          case "r" => createDocPagesAndRedirects(true)
          case "u" => createDocPagesAndRedirects(false)
          case _ => wrongInputExit()
        }
      case _ => wrongInputExit()
    }
  }

  private def wrongInputExit(): Unit = {
    println("Unexpected input. Exiting...")
    System.exit(1)
  }

  private def printLinks(printAll: Boolean): Unit = {
    val sparkOperationsByCategory = mapByCategory(sparkOperations())
    printOperationSiteLinks(sparkOperationsByCategory, printAll)
    printOperationMenuLinks(sparkOperationsByCategory, printAll)
  }

  private def createDocPagesAndRedirects(forceUpdate: Boolean): Unit = {
    val sparkOps = sparkOperations()
    val redirectCount = createRedirects(sparkOps, forceUpdate)
    val pageCount = createDocPages(sparkOps, forceUpdate)

    if(redirectCount == 0) {
      println("No redirects updated.")
    } else {
      println(s"Updated $redirectCount redirects.")
    }
    if(pageCount == 0) {
      println("No pages updated.")
    } else {
      println(s"Updated $pageCount pages.")
    }
  }
  // scalastyle:on println
}
