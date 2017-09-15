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

package io.deepsense.deeplang.doperations.examples

import scala.collection.mutable

import org.apache.spark.sql.Row
import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperables.Projector
import io.deepsense.deeplang.doperables.dataframe.DataFrame

object ExampleHtmlFormatter {
  def exampleHtml(
      operation: DOperation,
      inputDataFrames: Seq[DataFrame],
      outputDataFrames: Seq[DataFrame]): String = {

    s"""|## Example
        |${paramsHtml(operation)}
        |### Input
        |
        |${inputHtml(inputDataFrames)}
        |
        |### Output
        |
        |${outputHtml(outputDataFrames)}
      """.stripMargin
  }

  private def paramsHtml(operation: DOperation): String =
    ParametersHtmlFormatter.toHtml(operation)

  private def inputHtml(dfs: Iterable[DataFrame]): String =
    dataFramesToHtml(dfs, "Input")

  private def outputHtml(dfs: Iterable[DataFrame]): String =
    dataFramesToHtml(dfs, "Output")

  private def dataFramesToHtml(dfs: Iterable[DataFrame], header: String): String = {
    if (dfs.size > 1) { // Print additional headers if there are many DFs
      dfs.zipWithIndex.map {
        case (df, idx) =>
          val dfHtml = DataFrameHtmlFormatter.toHtml(df)
          s"""
             |#### $header $idx
             |
             |$dfHtml
             |""".
            stripMargin
      }.mkString("\n")
    } else {
      DataFrameHtmlFormatter.toHtml(dfs.head)
    }
  }
}

object DataFrameHtmlFormatter {
  def toHtml(dataFrame: DataFrame): String = {
    val names = dataFrame.sparkDataFrame.schema.map(_.name)
    val rows = dataFrame.sparkDataFrame.collect().toSeq.map(stringifyRow)

    s"""|<table class=\"table\">
        |  <thead>
        |${tabelarizeHeader(names)}
        |  </thead>
        |  <tbody>
        |${tabelarizeRows(rows)}
        |  </tbody>
        |</table>""".stripMargin
  }

  private def tabelarize(
      rows: Seq[Seq[String]],
      preRow: String,
      postRow: String,
      preValue: String,
      postValue: String): String = {

    def toHtmlValue(value: String): String = s"$preValue$value$postValue"
    def toHtmlRow(row: Seq[String]): String =
      s"""|$preRow
          |${row.mkString("\n")}
          |$postRow""".stripMargin

    rows.map{ r => toHtmlRow(r.map(toHtmlValue)) }.mkString("\n")
  }

  private def tabelarizeHeader(names: Seq[String]): String = {
    val preRow = "    <tr>"
    val preValue = "      <th>"
    val postValue = "</th>"
    val postRow = "    </tr>"
    tabelarize(Seq(names), preRow, postRow, preValue, postValue)
  }

  private def tabelarizeRows(rows: Seq[Seq[String]]): String = {
    val preRow = "    <tr>"
    val preValue = "      <td>"
    val postValue = "</td>"
    val postRow = "    </tr>"
    tabelarize(rows, preRow, postRow, preValue, postValue)
  }

  private def stringifyValue(value: Any): String = Option(value) match {
    case Some(nonNullValue) => nonNullValue match {
      case v: mutable.WrappedArray[_] => s"[${v.mkString(",")}]"
      case x => x.toString
    }
    case None => "null"
  }

  private def stringifyRow(row: Row): Seq[String] =
    row.toSeq.map(stringifyValue)
}

object ParametersHtmlFormatter {
  private val leftColumnFieldName: String = "left column"
  private val rightColumnFieldName: String = "right column"
  private val typeFieldName: String = "type"
  private val valueFieldName: String = "value"
  private val valuesFieldName: String = "values"
  private val selectionsFieldName: String = "selections"

  def toHtml(dOperation: DOperation): String = {
    if (dOperation.paramValuesToJson.asJsObject.fields.isEmpty) {
      "\n"
    } else {
      val paramValues = extractParamValues(dOperation.paramValuesToJson.asJsObject).toMap
      val paramsOrder = extractParamsOrder(dOperation.paramsToJson.asInstanceOf[JsArray])
      val orderedValues = paramsOrder.flatMap {
          case paramName =>
            paramValues.get(paramName).map(v => paramName -> v)
      }

      val paramsHtml = orderedValues.map {
          case (paramName, paramValue) =>
            paramValueToHtml(paramName, paramValue)
      }

      s"""|
          |### Parameters
          |
          |<table class="table">
          |  <thead>
          |    <tr>
          |      <th style="width:20%">Name</th>
          |      <th style="width:80%">Value</th>
          |    </tr>
          |  </thead>
          |  <tbody>
          |${paramsHtml.mkString("\n")}
          |  </tbody>
          |</table>
          |""".stripMargin
    }
  }

  private def extractParamsOrder(json: JsArray): Seq[String] = {
    json.elements.flatMap { case param =>
      val paramObject = param.asJsObject
      val subParams = if (paramObject.fields.contains(typeFieldName) &&
        paramObject.fields(typeFieldName).asInstanceOf[JsString].value == "choice") {
        val choices = paramObject.fields(valuesFieldName).asInstanceOf[JsArray]
          .elements.map(_.asJsObject).toSeq

        choices.flatMap { case c =>
          extractParamsOrder(c.fields("schema").asInstanceOf[JsArray])
        }
      } else {
        Seq()
      }

      paramObject.fields("name").asInstanceOf[JsString].value +:
        subParams.distinct
    }
  }

  private def extractParamValues(jsObject: JsObject): Seq[(String, String)] = {
    jsObject.fields.toSeq.flatMap {
      case (name, value: JsArray) if isColumnPairs(value) =>
        Seq(handleColumnPairs(name, value))
      case (name, value: JsArray) if isColumnProjection(value) =>
        Seq(handleColumnProjection(name, value))
      case (name, value: JsObject) if value.fields.size == 1 =>
        val (fieldName, innerFieldObject) = value.fields.head
        (name, fieldName) +: extractParamValues(innerFieldObject.asJsObject)
      case (name, value: JsObject) if isMultipleColumnSelection(name, value) =>
        Seq(handleMultipleColumnSelection(name, value))
      case (name, value: JsObject) if isSingleColumnSelection(name, value) =>
        Seq(handleSingleColumnSelection(name, value))
      case (name, value: JsValue) => value match {
        case JsNumber(_) | JsString(_) | JsBoolean(_) => Seq((name, value.toString))
      }
    }
  }

  private def paramValueToHtml(name: String, value: String): String = {
    s"""|  <tr>
        |    <td><code>$name</code></td>
        |    <td>$value</td>
        |  </tr>""".stripMargin
  }

  private def isColumn(jsObject: JsObject): Boolean = {
    jsObject.fields.contains(typeFieldName) &&
      jsObject.fields(typeFieldName).isInstanceOf[JsString]
    jsObject.fields(typeFieldName).asInstanceOf[JsString].value == "column" &&
      jsObject.fields.contains(valueFieldName)
  }

  private def isColumnPairs(value: JsArray): Boolean = {
    value.elements.forall { v =>
      v.isInstanceOf[JsObject] &&
        v.asJsObject.fields.contains(leftColumnFieldName) &&
        v.asJsObject.fields.contains(rightColumnFieldName) &&
        v.asJsObject.fields(leftColumnFieldName).isInstanceOf[JsObject] &&
        v.asJsObject.fields(rightColumnFieldName).isInstanceOf[JsObject] &&
        isColumn(v.asJsObject.fields(leftColumnFieldName).asJsObject) &&
        isColumn(v.asJsObject.fields(rightColumnFieldName).asJsObject)
    }
  }

  private def isColumnProjection(value: JsArray): Boolean = {
    value.elements.forall { v =>
      v.isInstanceOf[JsObject] &&
        v.asJsObject.fields.contains(Projector.OriginalColumnParameterName) &&
        v.asJsObject.fields(Projector.OriginalColumnParameterName).isInstanceOf[JsObject] &&
        isColumn(v.asJsObject.fields(Projector.OriginalColumnParameterName).asJsObject)
    }
  }

  private def extractColumnName(v: JsValue, key: String): String = {
    v.asJsObject
      .fields(key)
      .asInstanceOf[JsObject]
      .fields(valueFieldName).asInstanceOf[JsString].value
  }

  private def handleColumnPairs(name: String, value: JsArray): (String, String) = {
    val pairs = value.elements.map { v =>
      val leftColumn = extractColumnName(v, leftColumnFieldName)
      val rightColumn = extractColumnName(v, rightColumnFieldName)
      s"left.$leftColumn == right.$rightColumn"
    }.mkString(", ")

    (name, s"Join on $pairs")
  }

  private def handleColumnProjection(name: String, value: JsArray): (String, String) = {
    val pairs = value.elements.map { v =>
      val originalColumn = extractColumnName(v, Projector.OriginalColumnParameterName)
      val renameDesc =
        if (v.asJsObject.fields.contains(Projector.RenameColumnParameterName) &&
            v.asJsObject.fields(Projector.RenameColumnParameterName)
              .asJsObject.fields.contains("Yes")) {
          " (renamed to <code>" +
            v.asJsObject.fields(Projector.RenameColumnParameterName).asJsObject
              .fields.get("Yes").get.asJsObject.fields(Projector.ColumnNameParameterName)
              .asInstanceOf[JsString].value +
            "</code>)"
        } else {
          ""
        }
      s"<code>$originalColumn</code>$renameDesc"
    }.mkString(", ")

    (name, s"Select columns: $pairs")
  }


  private def isMultipleColumnSelection(name: String, value: JsObject): Boolean = {
    value.fields.size == 2 &&
      value.fields.contains(selectionsFieldName) &&
      value.fields.contains("excluding")
  }

  private def handleMultipleColumnSelection(name: String, value: JsObject): (String, String) = {
    val rawSelections = value.fields(selectionsFieldName).asInstanceOf[JsArray]
    val selections = rawSelections.elements.map(_.asJsObject).map {
      case s: JsObject if s.fields(typeFieldName).asInstanceOf[JsString].value == "columnList" =>
        val selectedColumns = s.fields(valuesFieldName).prettyPrint
        "by name: " + selectedColumns
    }
    (name, s"Selected columns: ${selections.mkString(", ")}.")
  }

  private def isSingleColumnSelection(name: String, value: JsObject): Boolean = {
    value.fields.size == 2 &&
      value.fields.contains(valueFieldName) &&
      value.fields.contains(typeFieldName)
  }

  private def handleSingleColumnSelection(name: String, value: JsObject): (String, String) = {
    (name, value.fields(valueFieldName).toString)
  }
}
