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

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.types.StructType
import org.mockito.Matchers._
import org.mockito.Mockito._

import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.{NumericParam, Param}
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, ExecutionContext, UnitSpec}

object MockDOperablesFactory extends UnitSpec {

  val DefaultForA = 1

  def mockTransformer(df: DataFrame, dfk: DKnowledge[DataFrame]): Transformer = {
    val t = mock[Transformer]
    val transform: DMethod1To1[Unit, DataFrame, DataFrame] =
      mock[DMethod1To1[Unit, DataFrame, DataFrame]]
    when(transform.apply(any())(any())(any())).thenReturn(df)
    when(transform.infer(any())(any())(any())).thenReturn((dfk, InferenceWarnings.empty))
    when(t.transform).thenReturn(transform)
    t
  }

  def dataFrameKnowledge(s: StructType): DKnowledge[DataFrame] = {
    val k = mock[DKnowledge[DataFrame]]
    when(k.types).thenReturn(Set(DataFrame.forInference(s)))
    k
  }

  val transformedDataFrame1 = mock[DataFrame]
  val transformedDataFrame2 = mock[DataFrame]
  val transformedDataFrameSchema1 = mock[StructType]
  val transformedDataFrameSchema2 = mock[StructType]
  val transformedDataFrameKnowledge1 = dataFrameKnowledge(transformedDataFrameSchema1)
  val transformedDataFrameKnowledge2 = dataFrameKnowledge(transformedDataFrameSchema2)
  val transformer1 = mockTransformer(transformedDataFrame1, transformedDataFrameKnowledge1)
  val transformer2 = mockTransformer(transformedDataFrame2, transformedDataFrameKnowledge2)
  val transformerKnowledge1 = DKnowledge(transformer1)
  val transformerKnowledge2 = DKnowledge(transformer2)

  val metricValue1 = MetricValue("name1", 0.1)
  val metricValue2 = MetricValue("name2", 0.2)

  val metricValueKnowledge1 = DKnowledge(MetricValue.forInference("name1"))
  val metricValueKnowledge2 = DKnowledge(MetricValue.forInference("name2"))

  class MockEstimator extends Estimator {
    val paramA = NumericParam("b", "desc")
    setDefault(paramA -> DefaultForA)
    override val params: Array[Param[_]] = declareParams(paramA)
    override def report: Report = ???

    override def fit: DMethod1To1[Unit, DataFrame, Transformer] = {
      new DMethod1To1[Unit, DataFrame, Transformer] {
        override def apply(context: ExecutionContext)(parameters: Unit)(t0: DataFrame)
        : Transformer = {
          $(paramA) match {
            case 1 => transformer1
            case 2 => transformer2
          }
        }

        override def infer
        (context: InferContext)
          (parameters: Unit)
          (k0: DKnowledge[DataFrame])
        : (DKnowledge[Transformer], InferenceWarnings) = {
          $(paramA) match {
            case 1 => (transformerKnowledge1, InferenceWarnings.empty)
            case 2 => (transformerKnowledge2, InferenceWarnings.empty)
          }
        }
      }
    }

    // Not used in tests.
    override private[deeplang] def _fit(ctx: ExecutionContext, df: DataFrame): Transformer = ???
    override private[deeplang] def _fit_infer(schema: Option[StructType]): Transformer = ???
  }

  class MockEvaluator extends Evaluator {
    val paramA = NumericParam("b", "desc")
    setDefault(paramA -> DefaultForA)
    override val params: Array[Param[_]] = declareParams(paramA)
    override def report: Report = ???

    override def evaluate: DMethod1To1[Unit, DataFrame, MetricValue] = {
      new DMethod1To1[Unit, DataFrame, MetricValue] {
        override def apply(ctx: ExecutionContext)(p: Unit)(dataFrame: DataFrame): MetricValue = {
          $(paramA) match {
            case 1 => metricValue1
            case 2 => metricValue2
          }
        }

        override def infer(ctx: InferContext)(p: Unit)(k: DKnowledge[DataFrame])
        : (DKnowledge[MetricValue], InferenceWarnings) = {
          $(paramA) match {
            case 1 => (metricValueKnowledge1, InferenceWarnings.empty)
            case 2 => (metricValueKnowledge2, InferenceWarnings.empty)
          }
        }
      }
    }

    // Not used in tests.
    private[deeplang] def _evaluate(context: ExecutionContext, dataFrame: DataFrame): MetricValue =
      ???
    private[deeplang] def _infer(k: DKnowledge[DataFrame]): MetricValue =
      ???

    override def isLargerBetter: Boolean = ???
  }
}
