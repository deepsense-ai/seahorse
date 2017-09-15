/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.akka

import akka.actor.{Address, ExtensionKey, Extension, ExtendedActorSystem}


class RemoteAddressExtensionImpl(system: ExtendedActorSystem) extends Extension {
  def address: Address = system.provider.getDefaultAddress
}

object RemoteAddressExtension extends ExtensionKey[RemoteAddressExtensionImpl]
