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

package io.deepsense.reportlib.model

case class Table(
  name: String,
  description: String,
  columnNames: Option[List[String]],
  rowNames: Option[List[String]],
  values: List[List[Option[String]]],
  blockType: String = Table.tableType) {
  require(blockType == Table.tableType)
}

object Table {
  val tableType = "table"
}
