package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Transformer

abstract class WriteReadTransformerIntegTest extends DeeplangIntegTestSupport {

  def writeReadTransformer(transformer: Transformer, outputFile: String): Unit = {

    val writeTransformer: WriteTransformer = WriteTransformer(outputFile)
    val readTransformer: ReadTransformer = ReadTransformer(outputFile)

    writeTransformer.executeUntyped(Vector(transformer))(executionContext)
    val deserializedTransformer =
      readTransformer.executeUntyped(Vector())(executionContext).head.asInstanceOf[Transformer]

    deserializedTransformer shouldBe transformer
  }
}
