/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.models.json.graph

import spray.json._

import io.deepsense.deeplang.doperables.descriptions.InferenceResult
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.{DKnowledge, DOperable}

trait DKnowledgeJsonProtocol extends DefaultJsonProtocol {

  implicit object DKnowledgeJsonFormat
    extends JsonFormat[DKnowledge[DOperable]]
    with InferenceResultJsonProtocol
    with DefaultJsonProtocol {

    override def write(dKnowledge: DKnowledge[DOperable]): JsValue = {
      val types = typeArray(dKnowledge)
      val result = inferenceResult(dKnowledge)

      JsObject(
        "types" -> types,
        "result" -> result.getOrElse(JsNull)
      )
    }

    def inferenceResult(dKnowledge: DKnowledge[DOperable]): Option[JsValue] = {
      if (dKnowledge.size != 1) {
        None
      } else {
        dKnowledge.single
          .inferenceResult
          .map(_.toJson)
      }
    }

    def typeArray(dKnowledge: DKnowledge[DOperable]): JsArray =
      JsArray(dKnowledge.types.map(_.getClass.getName.toJson).toVector)

    override def read(json: JsValue): DKnowledge[DOperable] = ???
  }
}

object DKnowledgeJsonProtocol extends DKnowledgeJsonProtocol
