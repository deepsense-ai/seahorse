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

package ai.deepsense.deeplang.params.multivalue

import spray.json._

case class ValuesSequenceParam[T](sequence: List[T]) extends MultipleValuesParam[T] {
  override def values(): List[T] = sequence
}

object ValuesSequenceParam {
  val paramType = "seq"
}

object ValuesSequenceParamJsonProtocol extends DefaultJsonProtocol {

  implicit def valuesSequenceParamFormat[A: JsonFormat]: RootJsonFormat[ValuesSequenceParam[A]] = {
    jsonFormat1(ValuesSequenceParam.apply[A])
  }
}
