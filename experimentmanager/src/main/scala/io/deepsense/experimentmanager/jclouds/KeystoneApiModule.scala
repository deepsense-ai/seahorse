/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.jclouds

import com.google.inject.{Singleton, Provides}
import com.google.inject.name.Named
import net.codingwell.scalaguice.ScalaModule
import org.jclouds.ContextBuilder
import org.jclouds.openstack.keystone.v2_0.KeystoneApi

class KeystoneApiModule extends ScalaModule {
  override def configure(): Unit = {
    // Configuration not needed - everything is done by the methods annotated with "Provides".
  }

  @Provides
  @Singleton
  def provideKeystoneApi(
    @Named("auth-service.endpoint") endpoint: String,
    @Named("auth-service.identity") identity: String,
    @Named("auth-service.password") password: String): KeystoneApi = {
    ContextBuilder.newBuilder("openstack-keystone")
      .endpoint(endpoint)
      .credentials(identity, password)
      .buildApi(classOf[KeystoneApi])
  }
}
