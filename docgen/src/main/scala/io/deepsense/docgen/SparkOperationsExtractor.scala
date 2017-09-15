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

package io.deepsense.docgen

import io.deepsense.deeplang.catalogs.doperations.{DOperationCategory, DOperationsCatalog}
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.SimpleSparkEstimatorWrapper
import io.deepsense.deeplang.doperations.{EstimatorAsFactory, EstimatorAsOperation, EvaluatorAsFactory, TransformerAsOperation}
import io.deepsense.deeplang.{CatalogRecorder, DOperation}

trait SparkOperationsExtractor {

  private val catalog = DOperationsCatalog()
  CatalogRecorder.registerDOperations(catalog)

  def sparkOperations(): Seq[OperationWithSparkClassName] = {
    val operationIds = catalog.operations.keys
    operationIds.map(operationId => catalog.createDOperation(operationId))
      .flatMap(operation =>
        sparkClassName(operation).map(OperationWithSparkClassName(operation, _))).toSeq
  }

  private def sparkClassName(operation: DOperation): Option[String] = {
    operation match {
      case (t: TransformerAsOperation[_]) =>
        t.transformer match {
          case (st: SparkTransformerWrapper[_]) =>
            Some(st.sparkTransformer.getClass.getCanonicalName)
          case (st: SparkTransformerAsMultiColumnTransformer[_]) =>
            Some(st.sparkTransformer.getClass.getCanonicalName)
          case _ => None
        }
      case e: (EstimatorAsFactory[_]) =>
        e.estimator match {
          case (se: SparkEstimatorWrapper[_, _, _]) =>
            Some(se.sparkEstimator.getClass.getCanonicalName)
          case (se: SparkMultiColumnEstimatorWrapper[_, _, _, _, _]) =>
            Some(se.sparkEstimatorWrapper.getClass.getCanonicalName)
          case (ses: SimpleSparkEstimatorWrapper[_]) =>
            Some(ses.sparkEstimator.getClass.getCanonicalName)
          case _ => None
        }
      case ev: (EvaluatorAsFactory[_]) =>
        ev.evaluator match {
          case (sev: SparkEvaluatorWrapper[_]) =>
            Some(sev.sparkEvaluator.getClass.getCanonicalName)
          case _ => None
        }
      case es: (EstimatorAsOperation[_]) =>
        es.estimator match {
          case (ses: SparkMultiColumnEstimatorWrapper[_, _, _, _, _]) =>
            Some(ses.sparkEstimatorWrapper.getClass.getCanonicalName)
          case (ses: SparkEstimatorWrapper[_, _, _]) =>
            Some(ses.sparkEstimator.getClass.getCanonicalName)
          case (ses: SimpleSparkEstimatorWrapper[_]) =>
            Some(ses.sparkEstimator.getClass.getCanonicalName)
          case _ => None
        }
      case _ => None
    }
  }

  def mapByCategory(operations: Seq[OperationWithSparkClassName])
      : Map[DOperationCategory, Seq[OperationWithSparkClassName]] = {

    val operationsWithCategories = operations.map(operationWithName => {
      val category = catalog.operations(operationWithName.op.id).category
      OperationWithCategory(category, operationWithName)
    })

    val categories = operationsWithCategories.map(_.category).toSet

    Map(categories.toList.sortBy(_.name).map(category =>
      category -> operationsWithCategories.filter(_.category == category).map(_.op)): _*)
  }

  private case class OperationWithCategory(
    category: DOperationCategory,
    op: OperationWithSparkClassName)
}
