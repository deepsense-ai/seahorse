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

package io.deepsense.deeplang.doperables

import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import io.deepsense.deeplang.doperables.multicolumn.MultiColumnEstimatorParams.SingleOrMultiColumnEstimatorChoices.{MultiColumnEstimatorChoice, SingleColumnEstimatorChoice}
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasInputColumn, HasOutputColumn}
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import io.deepsense.deeplang.{ExecutionContext, TypeUtils}

abstract class SparkMultiColumnEstimatorWrapper[
  MD <: ml.Model[MD],
  E <: ml.Estimator[MD],
  MW <: SparkSingleColumnModelWrapper[MD, E],
  EW <: SparkSingleColumnEstimatorWrapper[MD, E, MW],
  MMW <: MultiColumnModel[MD, E, MW]]
(implicit val modelWrapperTag: TypeTag[MW],
  implicit val estimatorTag: TypeTag[E],
  implicit val estimatorWrapperTag: TypeTag[EW],
  implicit val multiColumnModelTag: TypeTag[MMW])
 extends MultiColumnEstimator
 with ParamsWithSparkWrappers {

  val sparkEstimatorWrapper: EW = createEstimatorWrapperInstance()


  override def handleSingleColumnChoice(
      df: DataFrame,
      single: SingleColumnEstimatorChoice): MW = {

    import sparkEstimatorWrapper._

    val estimator = sparkEstimatorWrapper.replicate()
      .set(inputColumn -> single.getInputColumn)
      .set(outputColumn -> single.getOutputColumn)

    estimator._fit(df).asInstanceOf[MW]
  }


  override def handleMultiColumnChoice(
      df: DataFrame,
      multi: MultiColumnEstimatorChoice): MMW = {
    val inputColumns = df.getColumnNames(multi.getInputColumns)
    val prefix = multi.getOutputColumnsPrefix

    val models = inputColumns.map {
      case inputColumnName =>
        import sparkEstimatorWrapper._
        val estimator = sparkEstimatorWrapper.replicate()
          .set(inputColumn -> NameSingleColumnSelection(inputColumnName))
          .set(outputColumn -> DataFrameColumnsGetter.prefixedColumnName(inputColumnName, prefix))
        estimator._fit(df).asInstanceOf[MW]
    }

    createMultiColumnModel()
      .setModels(models)
      .setInputColumns(multi.getInputColumns)
      .setOutputColumnsPrefix(multi.getOutputColumnsPrefix)
  }


  override def handleSingleColumnChoiceInfer(
      schema: Option[StructType],
      single: SingleColumnEstimatorChoice): Transformer with HasInputColumn with HasOutputColumn = {

    import sparkEstimatorWrapper._

    sparkEstimatorWrapper.replicate()
      .set(inputColumn -> single.getInputColumn)
      .set(outputColumn -> single.getOutputColumn)
      ._fit_infer(schema).asInstanceOf[Transformer  with HasInputColumn with HasOutputColumn]
  }

  override def handleMultiColumnChoiceInfer(
      schema: Option[StructType],
     multi: MultiColumnEstimatorChoice): MMW = {

    schema.map { s =>

      val inputColumns = DataFrameColumnsGetter.getColumnNames(s, multi.getInputColumns)
      val prefix = multi.getOutputColumnsPrefix

      val models = inputColumns.map {
        case inputColumnName =>
          import sparkEstimatorWrapper._
          sparkEstimatorWrapper.replicate()
            .set(inputColumn -> NameSingleColumnSelection(inputColumnName))
            .set(outputColumn -> DataFrameColumnsGetter.prefixedColumnName(inputColumnName, prefix))
            ._fit_infer(Some(s)).asInstanceOf[MW]
      }

      createMultiColumnModel()
        .setModels(models)
        .setInputColumns(multi.getInputColumns)
        .setOutputColumnsPrefix(multi.getOutputColumnsPrefix)
    }.getOrElse(createMultiColumnModel()
      .setModels(Seq.empty))
  }

  override def report(executionContext: ExecutionContext): Report = Report()

  def createEstimatorWrapperInstance(): EW = TypeUtils.instanceOfType(estimatorWrapperTag)

  def createMultiColumnModel(): MMW = TypeUtils.instanceOfType(multiColumnModelTag)

}
