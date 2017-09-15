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

package io.deepsense.workflowexecutor.communication.message.global

import spray.json._

case class PythonGatewayAddress(addresses: List[Address])

case class Address(hostname: String, port: Int)

trait AddressJsonProtocol extends DefaultJsonProtocol {
  implicit val addressFormat = jsonFormat2(Address)
}

trait PythonGatewayAddressJsonProtocol extends DefaultJsonProtocol with AddressJsonProtocol {
  implicit val pythonGatewayAddressFormat = jsonFormat1(PythonGatewayAddress)
}

object PythonGatewayAddressJsonProtocol extends PythonGatewayAddressJsonProtocol
