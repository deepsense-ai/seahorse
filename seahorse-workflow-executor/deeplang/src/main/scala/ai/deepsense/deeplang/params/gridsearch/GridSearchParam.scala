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

package ai.deepsense.deeplang.params.gridsearch

import ai.deepsense.deeplang.params.{ParameterType, DynamicParam}
import ai.deepsense.deeplang.params.ParameterType._

class GridSearchParam(
    override val name: String,
    override val description: Option[String],
    override val inputPort: Int)
  extends DynamicParam(name, description, inputPort) {

  override val parameterType: ParameterType = ParameterType.GridSearch

  override def replicate(name: String): GridSearchParam =
    new GridSearchParam(name, description, inputPort)
}
