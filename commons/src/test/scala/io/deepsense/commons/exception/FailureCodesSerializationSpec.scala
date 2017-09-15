/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.exception

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.exception.FailureCode.FailureCode
import io.deepsense.commons.serialization.Serialization

class FailureCodesSerializationSpec extends StandardSpec with Serialization {
  "FailureCode" should {
    "serialize and deserialize" in {
      val code = FailureCode.UnexpectedError
      val serialized = serialize(code)
      val deserialized = deserialize[FailureCode](serialized)
      deserialized shouldBe code
    }
  }
}
