/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.auth

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

import io.deepsense.commons.auth.usercontext.{MockedTokenTranslator, KeystoneTokenTranslator, TokenTranslator}

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
