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

package io.deepsense.workflowexecutor.communication

import spray.json._

case class SingleAddress(hostname: String, port: Int)

case class PythonGatewayAddress(addresses: List[SingleAddress]) extends WriteMessageMQ {
  override protected def jsMessageType: JsValue = JsString(PythonGatewayAddress.messageType)

  override protected def jsMessageBody: JsValue =
    PythonGatewayAddressJsonProtocol.pythonGatewayAddressFormat.write(this)
}

object PythonGatewayAddress {
  val messageType: String = "pythonGatewayAddress"
}

trait SingleAddressJsonProtocol extends DefaultJsonProtocol {
  implicit val addressFormat = jsonFormat2(SingleAddress.apply)
}

trait PythonGatewayAddressJsonProtocol extends DefaultJsonProtocol with SingleAddressJsonProtocol {
  implicit val pythonGatewayAddressFormat = jsonFormat1(PythonGatewayAddress.apply)
}

object PythonGatewayAddressJsonProtocol extends PythonGatewayAddressJsonProtocol
