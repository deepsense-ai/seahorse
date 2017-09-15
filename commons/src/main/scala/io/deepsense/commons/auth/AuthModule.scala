/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.auth

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

import io.deepsense.commons.auth.usercontext.{KeystoneTokenTranslator, TokenTranslator}

class AuthModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[TokenTranslator]).to(classOf[KeystoneTokenTranslator])
    install(new FactoryModuleBuilder()
      .implement(classOf[Authorizator], classOf[UserContextAuthorizator])
      .build(classOf[AuthorizatorProvider]))
  }
}
