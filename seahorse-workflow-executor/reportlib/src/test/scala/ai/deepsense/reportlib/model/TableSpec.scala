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

package ai.deepsense.reportlib.model

import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.commons.types.ColumnType

class TableSpec extends WordSpec with Matchers  {

  "Table" should {
    "throw IllegalArgumentException" when {
      "created with columnNames and columnTypes of different size" in {
        an[IllegalArgumentException] should be thrownBy
          Table(
            "Name",
            "Description",
            Some(List("col1", "col2")),
            List(ColumnType.string, ColumnType.string, ColumnType.string),
            None,
            List(
              List(Some("v1"), None, None))
          )
      }
      "created one data row of size different than columnTypes size" in {
        an[IllegalArgumentException] should be thrownBy
          Table(
            "Name",
            "Description",
            Some(List("col1", "col2", "col3")),
            List(ColumnType.string, ColumnType.string, ColumnType.string),
            None,
            List(
              List(Some("v1"), None))
          )
      }
    }
    "get created" when {
      "no column names are passed" in {
        Table(
          "Name",
          "Description",
          None,
          List(ColumnType.string, ColumnType.string, ColumnType.string),
          None,
          List(
            List(Some("v1"), None, None))
        )
        info("Table created")
      }
      "no data rows are passed" in {
        Table(
          "Name",
          "Description",
          None,
          List(ColumnType.string, ColumnType.string, ColumnType.string),
          None,
          List()
        )
        info("Table created")
      }
    }
  }
}
