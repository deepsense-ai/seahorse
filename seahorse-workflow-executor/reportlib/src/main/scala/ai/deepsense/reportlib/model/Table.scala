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

import ai.deepsense.commons.types.ColumnType.ColumnType

case class Table(
  name: String,
  description: String,
  columnNames: Option[List[String]],
  columnTypes: List[ColumnType],
  rowNames: Option[List[String]],
  values: List[List[Option[String]]]) {
  require(columnNames match {
    case Some(columnNamesList) => columnNamesList.size == columnTypes.size
    case _ => true
  }, "columnNames and columnTypes should have the same size")
  require(values.filter(_.length != columnTypes.length).isEmpty,
    "at least one data row has size different than columnTypes size")
}

object Table {
  val tableType = "table"
}
