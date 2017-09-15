/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.jclouds

import java.util.Properties

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import org.jclouds.Constants.{PROPERTY_CONNECTION_TIMEOUT, PROPERTY_SO_TIMEOUT}
import org.jclouds.ContextBuilder
import org.jclouds.openstack.keystone.v2_0.KeystoneApi

class KeystoneApiModule extends AbstractModule {
  override def configure(): Unit = {
    // Configuration not needed - everything is done by the methods annotated with "Provides".
  }

  @Provides
  @Singleton
  def provideKeystoneApi(
      @Named("auth-service.endpoint") endpoint: String,
      @Named("auth-service.identity") identity: String,
      @Named("auth-service.password") password: String,
      @Named("auth-service.timeout.connection") connectionTimeout: Long,
      @Named("auth-service.timeout.socket") socketTimeout: Long): KeystoneApi = {
    val overrides = new Properties()
    overrides.setProperty(PROPERTY_CONNECTION_TIMEOUT, connectionTimeout.toString)
    overrides.setProperty(PROPERTY_SO_TIMEOUT, socketTimeout.toString)
    ContextBuilder.newBuilder("openstack-keystone")
      .endpoint(endpoint)
      .credentials(identity, password)
      .overrides(overrides)
      .buildApi(classOf[KeystoneApi])
  }
}
