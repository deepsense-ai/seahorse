/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager.rest

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.routing.PathMatchers

import io.deepsense.commons.auth.AuthorizatorProvider
import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.rest.{RestApi, RestComponent}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.experimentmanager.rest.json.DeepLangJsonProtocol

/**
 * Provides REST for operations.
 */
class OperationsApi @Inject() (
    val tokenTranslator: TokenTranslator,
    dOperableCatalog: DOperableCatalog,
    dOperationsCatalog: DOperationsCatalog,
    authorizatorProvider: AuthorizatorProvider,
    @Named("operations.api.prefix") apiPrefix: String)
    (implicit ec: ExecutionContext)
  extends RestApi
  with RestComponent
  with DeepLangJsonProtocol
  with SprayJsonSupport {

  require(StringUtils.isNotBlank(apiPrefix))

  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
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
                implicit val operationsFormat = DOperationDescriptorFullFormat
                dOperationsCatalog.operations.get(operationId) match {
                  case Some(operation) => complete(Future.successful(operation))
                  case None => complete(
                    StatusCodes.NotFound, s"Operation with id = $operationId does not exist")
                }
              }
            }
          } ~
          pathEndOrSingleSlash {
            get {
              implicit val operationsFormat = DOperationDescriptorBaseFormat
              withUserContext { userContext =>
                complete(Future.successful(dOperationsCatalog.operations))
              }
            }
          }
        }
      }
    }
  }
}
