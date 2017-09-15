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

package ai.deepsense.models.json.graph

import spray.json._

import ai.deepsense.deeplang.{DKnowledge, DOperable}

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

    // FIXME: This isn't much better deserialization than the previous
    // FIXME: '???' placeholder, but at least it doesn't throw an exception.
    // FIXME: We should consider in a near future (today is 16.11.2016) to
    // FIXME: implement it properly or find some other means to not silently
    // FIXME: discard the information contained inside passed json value.
    override def read(json: JsValue): DKnowledge[DOperable] = DKnowledge()

  }
}

object DKnowledgeJsonProtocol extends DKnowledgeJsonProtocol
