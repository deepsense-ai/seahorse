/**
 * Copyright (c) 2015, CodiLime Inc.
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
