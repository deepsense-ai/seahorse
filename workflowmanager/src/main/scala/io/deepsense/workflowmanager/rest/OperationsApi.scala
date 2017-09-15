/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.routing.{PathMatchers, Route}

import io.deepsense.commons.auth.AuthorizatorProvider
import io.deepsense.commons.auth.directives.{AuthDirectives, AbstractAuthDirectives, InsecureAuthDirectives}
import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.json.envelope.Envelope
import io.deepsense.commons.rest.{RestApiAbstractAuth, RestApi, RestComponent}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.models.json.workflow.DeepLangJsonProtocol

/**
 * Provides REST for operations.
 */
abstract class OperationsApi @Inject() (
    val tokenTranslator: TokenTranslator,
    dOperableCatalog: DOperableCatalog,
    dOperationsCatalog: DOperationsCatalog,
    authorizatorProvider: AuthorizatorProvider,
    @Named("operations.api.prefix") apiPrefix: String)
    (implicit ec: ExecutionContext)
  extends RestApiAbstractAuth
  with RestComponent
  with DeepLangJsonProtocol
  with SprayJsonSupport
  with Cors {

  self: AbstractAuthDirectives =>

  require(StringUtils.isNotBlank(apiPrefix))

  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route: Route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        cors {
          pathPrefix(pathPrefixMatcher) {
            path("hierarchy") {
              get {
                withUserContext { userContext =>
                  complete(Future.successful(dOperableCatalog.descriptor))
                }
              }
            } ~
            path("catalog") {
              get {
                withUserContext { userContext =>
                  complete(Future.successful(dOperationsCatalog.categoryTree))
                }
              }
            } ~
            path(JavaUUID) { operationId =>
              get {
                withUserContext { userContext =>
                  dOperationsCatalog.operations.get(operationId) match {
                    case Some(operation) => complete(Future.successful(Envelope(operation)))
                    case None => complete(
                      StatusCodes.NotFound, s"Operation with id = $operationId does not exist")
                  }
                }
              }
            } ~
            pathEndOrSingleSlash {
              get {
                withUserContext { userContext =>
                  complete(Future.successful(Envelope(dOperationsCatalog.operations)))
                }
              }
            }
          }
        }
      }
    }
  }
}

class SecureOperationsApi @Inject() (
    tokenTranslator: TokenTranslator,
    dOperableCatalog: DOperableCatalog,
    dOperationsCatalog: DOperationsCatalog,
    authorizatorProvider: AuthorizatorProvider,
    @Named("operations.api.prefix") apiPrefix: String)
    (implicit ec: ExecutionContext)
  extends OperationsApi(
    tokenTranslator,
    dOperableCatalog,
    dOperationsCatalog,
    authorizatorProvider,
    apiPrefix)
  with AuthDirectives

class InsecureOperationsApi @Inject() (
  tokenTranslator: TokenTranslator,
  dOperableCatalog: DOperableCatalog,
  dOperationsCatalog: DOperationsCatalog,
  authorizatorProvider: AuthorizatorProvider,
  @Named("operations.api.prefix") apiPrefix: String)
  (implicit ec: ExecutionContext)
  extends OperationsApi(
    tokenTranslator,
    dOperableCatalog,
    dOperationsCatalog,
    authorizatorProvider,
    apiPrefix)
  with InsecureAuthDirectives
