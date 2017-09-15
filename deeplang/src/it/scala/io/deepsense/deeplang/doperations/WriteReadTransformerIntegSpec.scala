package io.deepsense.deeplang.doperations

import java.nio.file.{Files, Path}

import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.{TargetTypeChoices, TypeConverter}

class WriteReadTransformerIntegSpec
  extends WriteReadTransformerIntegTest
  with BeforeAndAfter {

  val tempDir: Path = Files.createTempDirectory("writeReadTransformer")

  "ReadTransformer" should {
    "read previously written Transformer" in {
      val transformer =
        new TypeConverter().setTargetType(TargetTypeChoices.BooleanTargetTypeChoice())
      val outputPath: Path = tempDir.resolve("TypeConverter")

      writeReadTransformer(transformer, outputPath.toString)
    }
  }

  after {
    tempDir.toFile.delete()
  }
}
