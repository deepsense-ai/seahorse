/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.scalatest.{Ignore, BeforeAndAfter}

import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport}

@Ignore
class ReadFileIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter {
  val testDir = "/tests/ReadFileTest"

  before {
    rawHdfsClient.delete(testDir, true)
    executionContext.hdfsClient.copyLocalFile("../deeplang/src/it/resources/csv/", testDir)
  }

  "ReadFile" should {
    "read csv file from HDFS" in {
      val operation = new ReadFile()
      operation.parameters.getStringParameter(ReadFile.pathParam).value =
        Some(testDir + "/sample.csv")
      operation.parameters.getStringParameter(ReadFile.lineSeparatorParam).value =
        Some("\n")

      val file = operation
        .execute(executionContext)(Vector.empty[DOperable])
        .head
        .asInstanceOf[File]

      val lines = file.rdd.get.collect()
      lines should have length 3

      lines(0) shouldBe "a1,b1,c1"
      lines(1) shouldBe "a2,b2,c2"
      lines(2) shouldBe "a3,b3,c3"
    }
  }

  it should {
    "read empty csv file from HDFS" in {
      val operation = new ReadFile()
      operation.parameters.getStringParameter(ReadFile.pathParam).value =
        Some(testDir + "/empty.csv")
      operation.parameters.getStringParameter(ReadFile.lineSeparatorParam).value =
        Some("\n")
      val file = operation
        .execute(executionContext)(Vector.empty[DOperable])
        .head
        .asInstanceOf[File]

      file.rdd.get.collect() should have length 0
    }
  }

  it should {
    "read csv file with lines separated by X sign from HDFS" in {
      val operation = new ReadFile()
      operation.parameters.getStringParameter(ReadFile.pathParam).value =
        Some(testDir + "/X_separated.csv")
      operation.parameters.getStringParameter(ReadFile.lineSeparatorParam).value =
        Some("X")

      val file = operation
        .execute(executionContext)(Vector.empty[DOperable])
        .head
        .asInstanceOf[File]

      val lines = file.rdd.get.collect()
      lines should have length 3
      lines(0).trim shouldBe "x1,x1,x1"
      lines(1).trim shouldBe "x2,x2,x2"
      lines(2).trim shouldBe "x3,x3,x3"
    }
  }
}
