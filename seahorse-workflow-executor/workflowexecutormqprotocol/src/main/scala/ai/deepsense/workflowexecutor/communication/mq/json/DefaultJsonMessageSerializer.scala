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

package ai.deepsense.workflowexecutor.communication.mq.json

import scala.reflect.ClassTag

import spray.json._

import Constants.JsonKeys._

class DefaultJsonMessageSerializer[T : JsonWriter : ClassTag](typeName: String)
  extends JsonMessageSerializer {

  val serialize: PartialFunction[Any, JsObject] = {
    case o if isHandled(o) => handle(o.asInstanceOf[T])
  }

  private def isHandled(obj: Any): Boolean = implicitly[ClassTag[T]].runtimeClass.isInstance(obj)

  private def handle(body: T): JsObject = JsObject(
    messageTypeKey -> JsString(typeName),
    messageBodyKey -> body.toJson
  )
}
