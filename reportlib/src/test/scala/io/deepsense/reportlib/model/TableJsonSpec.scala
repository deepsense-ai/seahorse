/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

import org.scalatest.{Matchers, WordSpec}
import spray.json._

import io.deepsense.reportlib.model.factory.TableTestFactory

class TableJsonSpec extends WordSpec with Matchers with TableTestFactory with ReportJsonProtocol {

  "Table" should {
    "serialize" when {
      val rowNames: List[String] = List("rowName1", "rowName2")
      val columnNames: List[String] = List("A", "B")
      val values: List[List[String]] = List(List("11", "12"), List("23", "34"))
      "columnsNames specified" in {
        val json = testTableWithLabels(Some(columnNames), None, values).toJson
        json shouldBe jsonTable(Some(columnNames), None, values)
      }
      "rowsNames specified" in {
        val json = testTableWithLabels(None, Some(rowNames), values).toJson
        json shouldBe jsonTable(None, Some(rowNames), values)
      }
      "rowsNames and columnNames specified" in {
        val json = testTableWithLabels(Some(columnNames), Some(rowNames), values).toJson
        json shouldBe jsonTable(Some(columnNames), Some(rowNames), values)
      }
      "is empty" in {
        val json = testEmptyTable.toJson
        json shouldBe jsonTable(None, None, List())
      }
    }
    "deserialize" when {
      "filled table" in {
        val columnNames: Some[List[String]] = Some(List("A", "B"))
        val rowNames: Some[List[String]] = Some(List("1", "2"))
        val values: List[List[String]] = List(List("a", "b"), List("c", "d"))
        val json = jsonTable(columnNames, rowNames, values)
        json.convertTo[Table] shouldBe testTableWithLabels(columnNames, rowNames, values)
      }
      "empty table" in {
        val json = jsonTable(None, None, List())
        json.convertTo[Table] shouldBe testTableWithLabels(None, None, List())
      }
    }
  }

  private def jsonTable(
    columnsNames: Option[List[String]],
    rowsNames: Option[List[String]],
    values: List[List[String]]): JsObject = JsObject(Map[String, JsValue](
    "name" -> JsString(TableTestFactory.tableName),
    "blockType" -> JsString("table"),
    "description" -> JsString(TableTestFactory.tableDescription),
    "columnNames" -> columnsNames
      .map(names => JsArray(names.map(JsString(_)).toVector)).getOrElse(JsNull),
    "rowNames" -> rowsNames
      .map(names => JsArray(names.map(JsString(_)).toVector)).getOrElse(JsNull),
    "values" -> JsArray(values.map(row => JsArray(row.map(JsString(_)).toVector)).toVector)
  ))

}
