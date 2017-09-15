/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.params.library

import spray.json.DefaultJsonProtocol.StringJsonFormat

import ai.deepsense.deeplang.params.{ParamWithJsFormat, ParameterType}
import ai.deepsense.deeplang.params.ParameterType.ParameterType

case class LoadFromLibraryParam(
    name: String,
    override val description: Option[String])
  extends ParamWithJsFormat[String] {

  override def replicate(name: String): LoadFromLibraryParam = copy(name = name)

  override val parameterType: ParameterType = ParameterType.LoadFromLibrary
}
