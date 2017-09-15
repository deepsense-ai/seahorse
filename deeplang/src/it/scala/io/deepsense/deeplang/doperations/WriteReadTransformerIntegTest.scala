package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Transformer

abstract class WriteReadTransformerIntegTest extends DeeplangIntegTestSupport {

  def writeReadTransformer(transformer: Transformer, outputFile: String): Unit = {

    val writeTransformer: WriteTransformer = WriteTransformer(outputFile)
    val readTransformer: ReadTransformer = ReadTransformer(outputFile)

    writeTransformer.execute(executionContext)(Vector(transformer))
    val deserializedTransformer =
      readTransformer.execute(executionContext)(Vector()).head.asInstanceOf[Transformer]

    deserializedTransformer shouldBe transformer
  }
}
