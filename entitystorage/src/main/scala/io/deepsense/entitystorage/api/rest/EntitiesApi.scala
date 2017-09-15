/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.api.rest

import scala.concurrent.ExecutionContext

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.http.StatusCodes
import spray.routing.{ExceptionHandler, PathMatchers, Route}
import spray.util.LoggingContext

import io.deepsense.commons.auth.AuthorizatorProvider
import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.json.ExceptionsJsonProtocol
import io.deepsense.commons.rest.{RestApi, RestComponent}
import io.deepsense.entitystorage.exceptions.EntityNotFoundException
import io.deepsense.entitystorage.json.EntityJsonProtocol
import io.deepsense.entitystorage.services.EntityService
import io.deepsense.models.entities.{Entity, EntityUpdate}

class EntitiesApi @Inject() (
    val tokenTranslator: TokenTranslator,
    entityService: EntityService,
    authorizatorProvider: AuthorizatorProvider,
    @Named("entities.api.prefix") apiPrefix: String,
    @Named("roles.entities.get") roleGet: String,
    @Named("roles.entities.update") roleUpdate: String,
    @Named("roles.entities.delete") roleDelete: String)
    (implicit ec: ExecutionContext)
  extends RestApi with RestComponent with EntityJsonProtocol with ExceptionsJsonProtocol {

  require(StringUtils.isNoneBlank(apiPrefix))
  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route: Route = handleRejections(rejectionHandler) {
    handleExceptions(exceptionHandler) {
      pathPrefix(pathPrefixMatcher) {
        path(JavaUUID / "report") { idParameter =>
          val entityId: Entity.Id = idParameter
          get {
            withUserContext { userContext =>
              complete(
                authorizatorProvider.forContext(userContext).withRole(roleGet) { userContext =>
                  entityService.getEntityReport(userContext.tenantId, idParameter).map {
                    case Some(entity) => Map("entity" -> entity)
                    case None => throw EntityNotFoundException(entityId)
                  }
                }
              )
            }
          }
        } ~
        path(JavaUUID) { idParameter =>
          val entityId: Entity.Id = idParameter
          put {
            withUserContext { userContext =>
              entity(as[EntityUpdate]) { entityUpdate =>
                complete(
                  authorizatorProvider.forContext(userContext).withRole(roleUpdate) {
                    userContext =>
                      entityService.updateEntity(
                          userContext.tenantId, entityId, entityUpdate).map {
                        case Some(entity) => Map("entity" -> entity)
                        case None => throw EntityNotFoundException(entityId)
                      }
                  }
                )
              }
            }
          } ~
          delete {
            withUserContext { userContext =>
              complete(
                authorizatorProvider.forContext(userContext).withRole(roleDelete) { userContext =>
                  entityService.deleteEntity(userContext.tenantId, entityId).map( _ => "")
                }
              )
            }
          }
        } ~
        pathEndOrSingleSlash {
          get {
            withUserContext { userContext =>
              complete(
                authorizatorProvider.forContext(userContext).withRole(roleGet) { userContext =>
                  entityService.getAll(userContext.tenantId).map { list => Map("entities" -> list) }
                }
              )
            }
          }
        }
      }
    }
  }

  override def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    super.exceptionHandler(log) orElse ExceptionHandler {
      case e: EntityNotFoundException =>
        complete(StatusCodes.NotFound, e.failureDescription)
    }
  }
}
