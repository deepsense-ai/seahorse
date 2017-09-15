/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import java.net.{Inet6Address, InetAddress, NetworkInterface}

import scala.util.Try

object HostAddressResolver {

  def getHostAddress(): String = {
    import collection.JavaConversions._
    Try {
      val interfaces = NetworkInterface.getNetworkInterfaces.toIterable
      interfaces.flatMap { n =>
        n.getInetAddresses.toIterable.filter {
          address =>
            !address.isInstanceOf[Inet6Address] &&
              !address.isLoopbackAddress &&
              !address.isLinkLocalAddress &&
              !address.isAnyLocalAddress &&
              !address.isMulticastAddress &&
              !(address.getHostAddress == "255.255.255.255")
        }
      }
    }.get.headOption.getOrElse(InetAddress.getByName("127.0.0.1")).getHostAddress
  }
}
