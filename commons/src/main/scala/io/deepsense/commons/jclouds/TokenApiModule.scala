/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.jclouds

import com.google.inject.{AbstractModule, Injector, Provides}
import org.jclouds.openstack.keystone.v2_0.KeystoneApi
import org.jclouds.openstack.keystone.v2_0.features.TokenApi

class TokenApiModule extends AbstractModule {
  override def configure(): Unit = {
    // All done by defining ProvidesMethods
  }

  @Provides
  def provideTokenApi(injector: Injector): TokenApi = {
    val keystoneApi = injector.getInstance(classOf[KeystoneApi])
    keystoneApi.getTokenApi.get()
  }
}
