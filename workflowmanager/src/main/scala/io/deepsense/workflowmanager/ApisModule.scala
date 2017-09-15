/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names.named

import io.deepsense.commons.auth.AuthModule
import io.deepsense.commons.auth.directives.InsecureAuthDirectives
import io.deepsense.commons.rest.{RestComponent, VersionApi}
import io.deepsense.models.json.workflow.GraphReaderModule
import io.deepsense.workflowmanager.deeplang.DeepLangModule
import io.deepsense.workflowmanager.rest._

/**
 * Configures all existing APIs.
 */
class ApisModule(withMockedSecurity: Boolean) extends AbstractModule {
  private lazy val apiBinder = Multibinder.newSetBinder(binder(), classOf[RestComponent])

  protected[this] def bindApi: LinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    bind(classOf[String]).annotatedWith(named("componentName")).toInstance("workflowmanager")

    install(new AuthModule(withMockedSecurity))
    install(new GraphReaderModule)
    install(new DeepLangModule)

    bindApi.to(classOf[InsecureWorkflowApi])
    bindApi.to(classOf[InsecureOperationsApi])
    bindApi.to(classOf[VersionApi])
  }
}

object ApisModule {
  val SUPPORTED_API_VERSION = "0.4.0"
}
