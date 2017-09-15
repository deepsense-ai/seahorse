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

package ai.deepsense.commons.auth

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

import ai.deepsense.commons.auth.usercontext.{KeystoneTokenTranslator, MockedTokenTranslator, TokenTranslator}

class AuthModule(useMockSecurity: Boolean) extends AbstractModule {
  override def configure(): Unit = {
    if (useMockSecurity) {
      bind(classOf[TokenTranslator]).to(classOf[MockedTokenTranslator])
    } else {
      bind(classOf[TokenTranslator]).to(classOf[KeystoneTokenTranslator])
    }
    install(new FactoryModuleBuilder()
      .implement(classOf[Authorizator], classOf[UserContextAuthorizator])
      .build(classOf[AuthorizatorProvider]))
  }
}
