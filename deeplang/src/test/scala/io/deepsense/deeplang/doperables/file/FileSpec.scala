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

package io.deepsense.deeplang.doperables.file

import io.deepsense.deeplang.UnitSpec

class FileSpec extends UnitSpec {

  "File" should {
    "produce proper report" in {
      val params = Map("Size" -> "3GB", "Rows" -> "100000")
      val file = File(None, Some(params))

      val tables = file.report.content.tables
      tables should have size 1
      val table = tables.head._2

      forAll(table.rowNames.get.zipWithIndex) { case (rowName, i) =>
        table.values(i) should have length 1
        table.values(i).head shouldBe Option(params(rowName))
      }
    }
  }
}
