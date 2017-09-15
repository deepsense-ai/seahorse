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

package io.deepsense.deeplang.doperables

import org.apache.spark.rdd.RDD


trait Reporter {
  import Reporter._

  /**
   * Evaluates predictions and produces a report.
   *
   * It does not assume the RDD is cached.
   * It caches it in case it's needed.
   */
  def report(predictionsAndLabels: RDD[(Double, Double)]): Report

  /**
   * Produces a cross-validation report with info about all folds.
   *
   * It does not assume RDDs in folds returned by createFold are cached.
   * It caches them in case it's needed.
   */
  def crossValidationReport(numberOfFolds: Int, createFold: Int => CrossValidationFold): Report = {
    val partialReports = (0 to numberOfFolds - 1).map( splitIndex =>
      createPartialReport(splitIndex, createFold(splitIndex))
    )

    mergePartialReports(partialReports)
  }

  protected def createPartialReport(splitIndex: Int, foldData: CrossValidationFold): PartialReport

  protected def mergePartialReports(partialReports: Seq[PartialReport]): Report
}

object Reporter {
  class PartialReport(
    splitIndex: Int,
    trainingDataFrameSize: Long,
    testDataFrameSize: Long)

  case class CrossValidationFold(
    trainingDataFrameSize: Long,
    testDataFrameSize: Long,
    predictionsAndLabels: RDD[(Double, Double)])
}
