/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.params.choice

import scala.reflect.runtime.universe._

import spray.json.{DeserializationException, JsObject}

import ai.deepsense.deeplang.params.exceptions.NoArgumentConstructorRequiredException
import ai.deepsense.deeplang.params.{AbstractParamSpec, Param}
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

abstract class AbstractChoiceParamSpec[T, U <: Param[T]] extends AbstractParamSpec[T, U] {

  protected def createChoiceParam[V <: Choice : TypeTag](
    name: String, description: String): Param[V]

  className should {
    "throw an exception when choices don't have no-arg constructor" in {
      a[NoArgumentConstructorRequiredException] should be thrownBy
        createChoiceParam[BaseChoice]("name", "description")
    }
    "throw an exception when unsupported choice is given" in {
      val graphReader = mock[GraphReader]
      a[DeserializationException] should be thrownBy
        createChoiceParam[ChoiceABC]("name", "description").valueFromJson(
          JsObject(
            "unsupportedClass" -> JsObject()
          ),
          graphReader
        )
    }
    "throw an exception when not all choices are declared" in {
      an[IllegalArgumentException] should be thrownBy
        createChoiceParam[ChoiceWithoutDeclaration]("name", "description")
    }
  }
}
