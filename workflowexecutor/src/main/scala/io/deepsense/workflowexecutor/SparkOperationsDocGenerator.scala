/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor

import java.io.{PrintWriter, File}

import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationsCatalog}
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperations.{EvaluatorAsFactory, EstimatorAsFactory, TransformerAsOperation}
import io.deepsense.deeplang.{CatalogRecorder, DOperation}

/**
 * This utitlity app is responsible for three things:
 *
 * 1. Prints out Spark documentation links suitable to paste in operations.md
 * 2. Prints out Spark documentation links suitable to paste in operationsmenu.html
 * 3. Creates redirects to Spark-ported operations
 */
object SparkOperationsDocGenerator {

  val sparkVersion = "1.5.2"
  val sparkDocPrefix = s"http://spark.apache.org/docs/$sparkVersion/api/scala/index.html#"

  val catalog = DOperationsCatalog()
  CatalogRecorder.registerDOperations(catalog)

  def main(args: Array[String]): Unit = {
    val operationIds = catalog.operations.keys

    val sparkOperationsWithClasses =
      operationIds.map(operationId => catalog.createDOperation(operationId))
        .flatMap(operation =>
          sparkClassName(operation).map(OperationWithSparkClassName(operation, _))).toSeq

    val sparkOperationsByCategory = mapByCategory(sparkOperationsWithClasses)

    printOperationSiteLinks(sparkOperationsByCategory)
    printOperationMenuLinks(sparkOperationsByCategory)
    createRedirects(sparkOperationsWithClasses)
  }

  // scalastyle:off println
  private def printOperationSiteLinks(
      sparkOperationsByCategory: Map[DOperationCategory, Seq[OperationWithSparkClassName]])
      : Unit = {
    println("==== LINKS FOR OPERATION SITE ====")
    printLinksByCategory(
      sparkOperationsByCategory,
      (url: String, opName: String) => "<li><a href=\"" + url + "\">" + opName + "</a></li>")
  }

  private def printOperationMenuLinks(
      sparkOperationsByCategory: Map[DOperationCategory, Seq[OperationWithSparkClassName]])
      : Unit = {
    println("==== LINKS FOR OPERATION MENU ====")
    printLinksByCategory(
      sparkOperationsByCategory,
      (url: String, opName: String) => s"* [$opName]($url)")
  }

  private def printLinksByCategory(
      sparkOperationsByCategory: Map[DOperationCategory, Seq[OperationWithSparkClassName]],
      createLink: (String, String) => String)
      : Unit = {

    sparkOperationsByCategory.foreach { case (category, opList) =>
      println(category.name)
      opList.toList.sortBy(_.op.name).foreach { case OperationWithSparkClassName(op, sparkClass) =>
        val url = sparkDocPrefix + sparkClass
        println(createLink(url, op.name))
      }
      println()
    }
  }

  private def createRedirects(sparkOperations: Seq[OperationWithSparkClassName]): Unit = {
    sparkOperations.foreach { case OperationWithSparkClassName(operation, sparkClassName) =>
      val redirectFile = new File("docs/uuid/" + operation.id + ".md")
      val writer = new PrintWriter(redirectFile)
      writer.println("---")
      writer.println("layout: redirect")
      writer.println("redirect: " + sparkDocPrefix + sparkClassName)
      writer.println("---")
      writer.flush()
      writer.close()
    }
  }
  // scalastyle:on println

  private def sparkClassName(operation: DOperation): Option[String] = {
    operation match {
      case (t: TransformerAsOperation[_]) => {
        t.transformer match {
          case (st: SparkTransformerWrapper[_]) => {
            val sparkTransformer = st.sparkTransformer
            Some(st.sparkTransformer.getClass.getCanonicalName)
          }
          case (st: SparkTransformerAsMultiColumnTransformer[_]) => {
            Some(st.sparkTransformer.getClass.getCanonicalName)
          }
          case _ => None
        }
      }
      case e: (EstimatorAsFactory[_]) => {
        e.estimator match {
          case (se: SparkEstimatorWrapper[_, _, _]) => {
            Some(se.sparkEstimator.getClass.getCanonicalName)
          }
          case (se: SparkMultiColumnEstimatorWrapper[_, _, _, _, _]) => {
            Some(se.sparkEstimatorWrapper.getClass.getCanonicalName)
          }
          case _ => None
        }
      }
      case ev: (EvaluatorAsFactory[_]) => {
        ev.evaluator match {
          case (sev: SparkEvaluatorWrapper[_]) => {
            Some(sev.sparkEvaluator.getClass.getCanonicalName)
          }
          case _ => None
        }
      }
      case _ => None
    }
  }

  private def mapByCategory(operations: Seq[OperationWithSparkClassName])
      : Map[DOperationCategory, Seq[OperationWithSparkClassName]] = {

    val operationsWithCategories = operations.map(operationWithName => {
      val category = catalog.operations(operationWithName.op.id).category
      OperationWithCategory(category, operationWithName)
    })

    val categories = operationsWithCategories.map(_.category).toSet

    Map(categories.toList.sortBy(_.name).map(category =>
      category -> operationsWithCategories.filter(_.category == category).map(_.op)): _*)
  }

  private case class OperationWithSparkClassName(op: DOperation, sparkClass: String)
  private case class OperationWithCategory(
    category: DOperationCategory,
    op: OperationWithSparkClassName)
}
