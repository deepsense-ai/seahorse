package io.deepsense.deeplang.doperations

import java.nio.file.{Files, Path}

import org.scalatest.BeforeAndAfter
import io.deepsense.deeplang.doperables.{PythonTransformer, TargetTypeChoices, TypeConverter}
import io.deepsense.deeplang.doperations.exceptions.DeepSenseIOException

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
  "WriteTransformer" should {
    "overwrite the previously written Transformer if the overwrite parameter is set to true" in {
      val transformer1 = new PythonTransformer()
      val transformer2 =
        new TypeConverter().setTargetType(TargetTypeChoices.BooleanTargetTypeChoice())
      val outputPath: Path = tempDir.resolve("TypeConverter")
      writeTransformer(transformer1, outputPath.toString, overwrite = true)
      writeReadTransformer(transformer2, outputPath.toString)
    }

    "throw an exception if a Transformer with the given name exists and the overwrite parameter is set to false" in {
      val transformer =
        new TypeConverter().setTargetType(TargetTypeChoices.BooleanTargetTypeChoice())
      val outputPath: Path = tempDir.resolve("TypeConverter")
      writeTransformer(transformer, outputPath.toString, overwrite = true)
      a [DeepSenseIOException] shouldBe thrownBy {
        writeTransformer(transformer, outputPath.toString, overwrite = false)
      }
    }
  }

  after {
    tempDir.toFile.delete()
  }
}
