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
