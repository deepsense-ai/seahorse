/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons

import java.io.NotSerializableException

import com.typesafe.scalalogging.{LazyLogging, Logger}

import io.deepsense.commons.serialization.Serialization
import io.deepsense.commons.utils.Logging

class LoggingSerializationSpec
  extends StandardSpec
  with UnitTestSupport
  with Serialization {

  "Object" when {
    "mixes-in SerializableLogging" should {
      "be serializable" in {
        val testObject = new SerializableTestObject()
        testObject.getLogger.trace("Logging just to force initiation of lazy logger")
        val deserialized = serializeDeserialize[SerializableTestObject](testObject)
        deserialized.getLogger should not be null
        deserialized.getLogger.trace("If this is printed everything is OK")
      }
    }
    "mixes-in standard LazyLogging" should {
      "not be serializable" in {
        val testObject = new NonSerializableTestObject()
        testObject.getLogger.trace("Logging just to force initiation of lazy logger")
        a[NotSerializableException] shouldBe
          thrownBy(serializeDeserialize[NonSerializableTestObject](testObject))
      }
    }
  }
}

class NonSerializableTestObject extends Serializable with LazyLogging {
  def getLogger: Logger = this.logger
}

class SerializableTestObject extends Serializable with Logging {
  def getLogger: Logger = this.logger
}
