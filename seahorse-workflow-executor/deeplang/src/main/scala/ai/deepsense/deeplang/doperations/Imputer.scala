/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package ai.deepsense.deeplang.doperations

import scala.language.reflectiveCalls
import scala.reflect.runtime.universe.TypeTag

import ai.deepsense.commons.models.Id
import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.InputerTransformer.{Mean, Strategy}
import ai.deepsense.deeplang.params.choice.Choice
import ai.deepsense.deeplang.params.{ColumnSelectorParam, Param}
import ai.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection}
import ai.deepsense.deeplang.params.wrappers.spark.{ChoiceParamWrapper, ParamsWithSparkWrappers}
import org.apache.spark.ml
import org.apache.spark.ml.feature.{Imputer => SparkImputer}
import org.apache.spark.sql.types.StructType



object InputerTransformer {
  sealed abstract class Strategy(override val name: String) extends Choice {
    override val params: Array[Param[_]] = Array()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Mean],
      classOf[Median]
    )
  }
  case class Mean() extends Strategy("mean")
  case class Median() extends Strategy("median")
}


class InputerTransformer extends Transformer with ParamsWithSparkWrappers {

  val selectedColumns = ColumnSelectorParam(
    name = "selected columns",
    description = Some("Columns to complete missing values in."),
    portIndex = 0)

  def getSelectedColumns: MultipleColumnSelection = $(selectedColumns)

  def setSelectedColumns(value: MultipleColumnSelection): this.type =
    set(selectedColumns, value)

  def setSelectedColumns(retainedColumns: Seq[String]): this.type =
    setSelectedColumns(
      MultipleColumnSelection(
        Vector(NameColumnSelection(retainedColumns.toSet)),
        excluding = false))

  val strategy = new ChoiceParamWrapper[
    ml.param.Params { val strategy: ml.param.Param[String] }, Strategy](
    name = "strategy",
    description = Some("Algorithm used to compute missing value"),
    sparkParamGetter = _.strategy)
  setDefault(strategy, Mean())

  override val params: Array[Param[_]] = Array(selectedColumns, strategy)



  override def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val columns = df.getColumnNames(getSelectedColumns)
    val inputer = new SparkImputer()
      .setInputCols(columns.toArray)
      .setOutputCols(columns.toArray)
    val sparkParams =
      sparkParamMap(inputer, df.sparkDataFrame.schema)

    val imputerModel = inputer.fit(df.sparkDataFrame, sparkParams)
    val res = imputerModel.transform(df.sparkDataFrame).toDF()
    DataFrame.fromSparkDataFrame(res)
  }

  override def applyTransformSchema(schema: StructType): Option[StructType] = {
    Some(schema)
  }
}


class Imputer extends TransformerAsOperation[InputerTransformer] with OperationDocumentation {
  override val id: Id = "37d8ce21-0aa9-4448-8fb0-defb58c5e53f"
  override val name: String = "Imputer"
  override val description: String =
    "Completes missing values using Estimator on a DataFrame"

  override lazy val tTagTI_0: TypeTag[DataFrame] = typeTag
  override lazy val tTagTO_0: TypeTag[DataFrame] = typeTag
  override lazy val tTagTO_1: TypeTag[InputerTransformer] = typeTag

  override val since: Version = Version(1, 5, 0)
}
