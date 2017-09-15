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

package ai.deepsense.deeplang.doperables.dataframe

object ColumnRole extends Enumeration {
  type ColumnRole = RichValue
  val Feature = RichValue("feature")
  val Label = RichValue("label")
  val Prediction = RichValue("prediction")
  val Id = RichValue("id")
  val Ignored = RichValue("ignored")

  case class RichValue(name: String) extends Val(nextId, name)

}
