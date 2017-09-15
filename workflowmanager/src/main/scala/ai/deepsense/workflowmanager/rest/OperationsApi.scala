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

package ai.deepsense.workflowmanager.rest

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.routing.{PathMatchers, Route}

import ai.deepsense.commons.auth.AuthorizatorProvider
import ai.deepsense.commons.auth.directives.{AbstractAuthDirectives, AuthDirectives, InsecureAuthDirectives}
import ai.deepsense.commons.auth.usercontext.TokenTranslator
import ai.deepsense.commons.json.envelope.Envelope
import ai.deepsense.commons.rest.{Cors, RestApiAbstractAuth, RestComponent}
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import ai.deepsense.models.json.workflow.DeepLangJsonProtocol

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
    cors {
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
