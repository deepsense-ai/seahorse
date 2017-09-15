/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.rest

import scala.concurrent.ExecutionContext

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import spray.routing.PathMatchers

import io.deepsense.commons.auth.AuthorizatorProvider
import io.deepsense.commons.auth.usercontext.TokenTranslator
import io.deepsense.commons.rest.{RestApi, RestComponent}
import io.deepsense.entitystorage.json.EntityJsonProtocol
import io.deepsense.entitystorage.models.{Entity, EntityDescriptor}
import io.deepsense.entitystorage.storage.EntityDao

class EntitiesApi @Inject() (
    val tokenTranslator: TokenTranslator,
    entityDao: EntityDao,
    authorizatorProvider: AuthorizatorProvider,
    @Named("entities.api.prefix") apiPrefix: String,
    @Named("roles.entities.get") roleGet: String)
    (implicit ec: ExecutionContext)
  extends RestApi with RestComponent with EntityJsonProtocol {

  require(StringUtils.isNoneBlank(apiPrefix))
  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  def route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        pathPrefix(pathPrefixMatcher) {
          get {
            withUserContext { userContext =>
              complete(
                authorizatorProvider.forContext(userContext).withRole(roleGet) { userContext =>
                  entityDao.getAll(userContext.tenantId).map(entitiesDescriptors)
                }
              )
            }
          }
        }
      }
    }
  }

  private def entitiesDescriptors(entities: List[Entity]):
      Map[String, Map[String, EntityDescriptor]] =
    Map("entities" -> entities.map(e => e.id.value.toString -> EntityDescriptor(e)).toMap)

}
