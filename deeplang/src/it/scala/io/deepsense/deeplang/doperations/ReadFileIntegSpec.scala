/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.{DOperable, DeeplangIntegTestSupport}

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
      val file = readFile("sample.csv", ReadFile.unixSeparatorLabel)
      val lines = file.rdd.get.collect()
      lines should have length 3
      lines(0) shouldBe "a1,b1,c1"
      lines(1) shouldBe "a2,b2,c2"
      lines(2) shouldBe "a3,b3,c3"
    }
  }

  it should {
    "read empty csv file from HDFS" in {
      val file = readFile("empty.csv", ReadFile.unixSeparatorLabel)
      file.rdd.get.collect() should have length 0
    }
  }

  it should {
    "read csv with lines separated by Windows CR+LF from HDFS" in {
      val file = readFile("win_sample.csv", ReadFile.windowsSeparatorLabel)
      val lines = file.rdd.get.collect()
      lines should have length 3
      lines(0) shouldBe "a1,b1,c1"
      lines(1) shouldBe "a2,b2,c2"
      lines(2) shouldBe "a3,b3,c3"
    }
  }

  it should {
    "not read the csv properly if incorrect line separator is chosen" in {
      val file = readFile("sample.csv", ReadFile.windowsSeparatorLabel)
      val lines = file.rdd.get.collect()
      lines should have length 1
    }
  }

  it should {
    "read csv file with lines separated by X sign from HDFS" in {
      // This test cannot use the readFile helper method as custom separator
      // needs different parameters
      val operation = new ReadFile()
      val parameters = operation.parameters
      parameters.getStringParameter(ReadFile.pathParam).value =
        Some(testDir + "/X_separated.csv")
      parameters.getChoiceParameter(ReadFile.lineSeparatorParam).value =
        Some(ReadFile.customLineSeparatorLabel)
      parameters.getChoiceParameter(ReadFile.lineSeparatorParam)
        .options
        .get(ReadFile.customLineSeparatorLabel)
        .get
        .getStringParameter(ReadFile.customLineSeparatorParam)
        .value = Some("X")

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

  def readFile(fileName: String, separator: String) : File = {
    val operation = new ReadFile()
    operation.parameters.getStringParameter(ReadFile.pathParam).value =
      Some(testDir + "/" + fileName)
    operation.parameters.getChoiceParameter(ReadFile.lineSeparatorParam).value =
      Some(separator)
    operation
      .execute(executionContext)(Vector.empty[DOperable])
      .head
      .asInstanceOf[File]
  }
}
