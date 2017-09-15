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
