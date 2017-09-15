/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.rest

import buildinfo.BuildInfo
import com.google.inject.Inject
import com.google.inject.name.Named
import spray.routing.Route

import io.deepsense.commons.auth.usercontext.TokenTranslator

class VersionApi @Inject() (
    @Named("componentName") val componentName: String,
    val tokenTranslator: TokenTranslator)
  extends RestApi with RestComponent {

  override def route: Route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        path("version") {
          get {
            complete(s"name: $componentName, version: ${BuildInfo.version}, " +
              s"scalaVersion: ${BuildInfo.scalaVersion}, sbtVersion: ${BuildInfo.sbtVersion}, " +
              s"gitCommitId: ${BuildInfo.gitCommitId}")
          }
        }
      }
    }
  }
}
