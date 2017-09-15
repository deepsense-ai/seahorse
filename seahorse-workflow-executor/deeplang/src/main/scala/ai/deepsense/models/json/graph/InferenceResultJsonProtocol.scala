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

import ai.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import ai.deepsense.deeplang.doperables.descriptions.{DataFrameInferenceResult, InferenceResult, ParamsInferenceResult}
import ai.deepsense.reportlib.model._

trait InferenceResultJsonProtocol
  extends DefaultJsonProtocol
  with StructTypeJsonProtocol {

  implicit object InferenceResultWriter extends RootJsonWriter[InferenceResult] {

    implicit val dataFrameInferenceResultFormat = jsonFormat1(DataFrameInferenceResult)

    implicit val paramsInferenceResultFormat = {
      implicit val baseFormat = jsonFormat2(ParamsInferenceResult)
      EnvelopeJsonFormat[ParamsInferenceResult]("params")
    }

    override def write(obj: InferenceResult): JsValue = {
      obj match {
        case d: DataFrameInferenceResult => d.toJson
        case p: ParamsInferenceResult => Envelope(p).toJson
      }
    }
  }
}

object InferenceResultJsonProtocol extends InferenceResultJsonProtocol

