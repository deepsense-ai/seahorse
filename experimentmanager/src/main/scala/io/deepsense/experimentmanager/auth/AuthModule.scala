/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.auth

import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

import io.deepsense.experimentmanager.auth.usercontext.{KeystoneTokenTranslator, TokenTranslator}

class AuthModule extends ScalaModule {
  override def configure(): Unit = {
    bind[TokenTranslator].to[KeystoneTokenTranslator]
    install(new FactoryModuleBuilder()
      .implement(classOf[Authorizator], classOf[UserContextAuthorizator])
      .build(classOf[AuthorizatorProvider]))
  }
}
