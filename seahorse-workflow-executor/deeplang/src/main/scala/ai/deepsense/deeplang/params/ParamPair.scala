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

case class ParamPair[T](param: Param[T], values: Seq[T]) {
  require(values.nonEmpty)
  values.foreach(param.validate)

  lazy val value = values.head
}

object ParamPair {

  def apply[T](param: Param[T], value: T): ParamPair[T] = {
    ParamPair(param, Seq(value))
  }
}
