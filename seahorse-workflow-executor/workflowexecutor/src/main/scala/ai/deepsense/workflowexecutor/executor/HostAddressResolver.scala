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

package ai.deepsense.workflowexecutor.executor

import java.net.{Inet6Address, InetAddress, NetworkInterface}

import scala.util.Try

import ai.deepsense.commons.utils.Logging

object HostAddressResolver extends Logging {

  def findHostAddress(): InetAddress = {
    import collection.JavaConversions._
    Try {
      val interfaces = NetworkInterface.getNetworkInterfaces.toIterable
      interfaces.flatMap { n =>
        n.getInetAddresses.toIterable.filter {
          address =>
            !address.isInstanceOf[Inet6Address] &&
            !address.isLoopbackAddress &&
            !address.isSiteLocalAddress &&
            !address.isLinkLocalAddress &&
            !address.isAnyLocalAddress &&
            !address.isMulticastAddress &&
            !(address.getHostAddress == "255.255.255.255")
        }
      }
    }.get.headOption.getOrElse(InetAddress.getByName("127.0.0.1"))
  }
}
