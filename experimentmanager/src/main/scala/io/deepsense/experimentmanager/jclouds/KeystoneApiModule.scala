/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.jclouds

import net.codingwell.scalaguice.ScalaModule
import org.jclouds.ContextBuilder
import org.jclouds.openstack.keystone.v2_0.KeystoneApi

class KeystoneApiModule extends ScalaModule {
  override def configure(): Unit = {
    // TODO Move to configuration
    val endpoint = "http://127.0.0.1:35357/v2.0/"
    val identity = "service:mtest"
    val password = "mpass"
    val provider = "openstack-keystone"
    val keystoneApi = ContextBuilder.newBuilder(provider)
      .endpoint(endpoint)
      .credentials(identity, password)
      .buildApi(classOf[KeystoneApi])

    bind[KeystoneApi].toInstance(keystoneApi)
  }
}
