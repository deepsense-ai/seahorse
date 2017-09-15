/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.rest

import buildinfo.BuildInfo
import com.google.inject.Inject
import spray.routing.Route

import io.deepsense.commons.auth.usercontext.TokenTranslator

class VersionApi @Inject() (
    val tokenTranslator: TokenTranslator)
  extends RestApi with RestComponent {

  override def route: Route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        path("version") {
          get {
            complete(BuildInfo.toString)
          }
        }
      }
    }
  }
}
