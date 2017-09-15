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

package ai.deepsense.deeplang.params

import spray.json.DefaultJsonProtocol.StringJsonFormat

import ai.deepsense.deeplang.exceptions.DeepLangException
import ai.deepsense.deeplang.params.validators.ColumnPrefixNameValidator

case class PrefixBasedColumnCreatorParam(
    override val name: String,
    override val description: Option[String])
  extends ParamWithJsFormat[String] {

  override def validate(value: String): Vector[DeepLangException] = {
    ColumnPrefixNameValidator.validate(name, value) ++ super.validate(value)
  }

  val parameterType = ParameterType.PrefixBasedColumnCreator

  override def replicate(name: String): PrefixBasedColumnCreatorParam = copy(name = name)
}

trait EmptyPrefixValidator extends PrefixBasedColumnCreatorParam {
  override def validate(value: String): Vector[DeepLangException] = {
    if (value.isEmpty) {
      Vector()
    } else {
      super.validate(value)
    }
  }
}
