/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager.app.rest

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.routing.PathMatchers

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._
import io.deepsense.experimentmanager.auth.AuthorizatorProvider
import io.deepsense.experimentmanager.auth.usercontext.TokenTranslator
import io.deepsense.experimentmanager.rest.RestComponent

/**
 * Provides REST for operations.
 */
class OperationsApi @Inject() (
    val tokenTranslator: TokenTranslator,
    dOperableCatalog: DOperableCatalog,
    authorizatorProvider: AuthorizatorProvider,
    @Named("roles.operations.hierarchy") hierarchyRole: String,
    @Named("operations.api.prefix") apiPrefix: String)
    (implicit ec: ExecutionContext)
  extends RestService
  with RestComponent {

  require(StringUtils.isNotBlank(apiPrefix))

  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        pathPrefix(pathPrefixMatcher) {
          path("hierarchy") {
            get {
              withUserContext { userContext =>
                complete(authorizatorProvider.forContext(userContext).withRole(hierarchyRole) { _ =>
                  Future.successful(dOperableCatalog.descriptor)
                })
              }
            }
          }
        }
      }
    }
  }
}
