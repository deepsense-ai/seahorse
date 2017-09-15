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

package ai.deepsense.deeplang.doperables.spark.wrappers.params.common

import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.Choice

sealed abstract class RegressionImpurity(override val name: String) extends Choice {
  import RegressionImpurity._

  override val params: Array[Param[_]] = Array()
  override val choiceOrder: List[Class[_ <: Choice]] = List(
    classOf[Variance]
  )
}

object RegressionImpurity {
  case class Variance() extends RegressionImpurity("variance")
}
