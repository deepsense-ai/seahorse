/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._

class FileToDataFrame extends DOperation1To1[File, DataFrame] {
  import FileToDataFrame._
  override val name: String = "File To DataFrame"
  override val id: Id = "83bad450-f87c-11e4-b939-0800200c9a66"
  override val parameters: ParametersSchema = ParametersSchema(
    formatParameter -> ChoiceParameter(
      "Format of the input file",
      Some(CSV.name),
      required = true,
      Map(CSV.name -> ParametersSchema(
        separatorParameter -> StringParameter(
          "Column separator",
          Some(","),
          required = true,
          new AcceptAllRegexValidator
        ),
        namesIncludedParameter -> BooleanParameter(
          "Does the first row include column names?",
          Some(true),
          required = true
        )
      ))),
    categoricalColumnsParameter -> ColumnSelectorParameter(
      "Categorical columns in the input File",
      required = false
    )
  )

  override protected def _execute(context: ExecutionContext)(file: File): DataFrame = ???
}

object FileToDataFrame {
  val formatParameter = "format"
  val separatorParameter = "separator"
  val categoricalColumnsParameter = "categoricalColumns"
  val namesIncludedParameter = "namesIncluded"

  sealed abstract class FileType(val name: String)

  object FileType {
    def forName(n: String): FileType = n.toLowerCase match {
      case "csv" => CSV
    }
  }

  case object CSV extends FileType("CSV")

}
