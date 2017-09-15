/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import java.net.{Inet6Address, InetAddress, NetworkInterface}

import scala.util.Try

import io.deepsense.commons.utils.Logging

object HostAddressResolver extends Logging {

  def getHostAddress(): String = {
    val address = if (System.getenv("MANAGED_BY") == "TAP") {
      logger.info("Getting host address from environment")
      getHostAddressFromEnv()
    } else {
      logger.info("Getting host address from network interface")
      getHostAddressFromInterface()
    }
    logger.info(s"Host address: $address")
    address
  }

  private def getHostAddressFromInterface(): String = {
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

  private def getHostAddressFromEnv(): String = {
    val hostname = System.getenv("HOSTNAME")
    val podId = hostname.toUpperCase.split("-").head
    val clusterIp = System.getenv(s"${podId}_SERVICE_HOST")
    clusterIp
  }
}
