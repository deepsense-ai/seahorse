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

package ai.deepsense.commons.auth.usercontext

import scala.collection.JavaConversions.asScalaSet
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import com.google.inject.Inject
import org.jclouds.openstack.keystone.v2_0.domain.{Role => KeystoneRole, Tenant => KeystoneTenant, Token => KeystoneToken, User => KeystoneUser}
import org.jclouds.openstack.keystone.v2_0.features.TokenApi
import org.jclouds.openstack.keystone.v2_0.{domain => keystone}

class KeystoneTokenTranslator @Inject()(
    tokenApi: TokenApi)
    (implicit ec: ExecutionContext)
  extends TokenTranslator {
  /**
   * Retrieves a user and a token from Keystone for the specified X-Auth-Token.
   * Converts the received data to format corresponding to UserContext.
   * The translation is done to separate Keystone models from other parts of the system
   * that use Auth module.
   * @param token Token received from the user (e.g. from X-Auth-Token header).
   * @return Future containing user's context. Returns a failed Future especially when
   *         could not retrieve the token from Keystone or the token is not linked
   *         to any tenant [[NoTenantSpecifiedException]] (the user is authenticated
   *         but without any tenant context).
   */
  override def translate(token: String): Future[UserContext] = {
    val tokenKeystoneFuture = tokenFromKeystone(token)
    val userKeystoneFuture = userFromKeystone(token)

    val rolesFuture: Future[Set[Role]] = userKeystoneFuture.map(rolesOfUser)
    val tenantFromToken = tenantFromKeystoneToken(token) _
    val tenantFuture: Future[Tenant] = tokenKeystoneFuture.map(tenantFromToken)

    val tokenFuture = for {
      tenant <- tenantFuture
      keystoneToken <- tokenKeystoneFuture
    } yield toToken(tenant, keystoneToken)

    val userFuture: Future[User] = userKeystoneFuture.map(toUser)

    val userContext = for {
      token <- tokenFuture
      tenant <- tenantFuture
      user <- userFuture
      roles <- rolesFuture
    } yield KeystoneUserContext(token, tenant, user, roles)

    userContext
  }

  private def tokenFromKeystone(token: String): Future[KeystoneToken] = {
    Future(Option(tokenApi.get(token))) flatMap {
      case Some(t) => Future.successful(t)
      case None => Future
        .failed[keystone.Token](
          new CannotGetTokenException(token))
    }
  }

  private def userFromKeystone(token: String): Future[KeystoneUser] = {
    Future(Option(tokenApi.getUserOfToken(token))) flatMap {
      case Some(u) => Future.successful(u)
      case None => Future
        .failed[keystone.User](
          new CannotGetUserException(token))
    }
  }

  private def rolesOfUser(keystoneUser: KeystoneUser): Set[Role] = {
    val keystoneRoleJavaSet: java.util.Set[KeystoneRole] = keystoneUser
    val keystoneRoles: Set[KeystoneRole] = asScalaSet(keystoneRoleJavaSet).toSet
    keystoneRoles.map(toRole)
  }

  private def toRole(keystoneRole: KeystoneRole): Role = {
    Role(keystoneRole.getName,
      Option(keystoneRole.getId),
      Option(keystoneRole.getDescription),
      Option(keystoneRole.getDescription),
      Option(keystoneRole.getTenantId))
  }

  private def tenantFromKeystoneToken(token: String)(keystoneToken: KeystoneToken): Tenant = {
    val tenant: Option[Tenant] = Option(keystoneToken.getTenant.orNull())
      .map(toTenant)
    if (tenant.isDefined) {
      tenant.get
    } else {
      throw new NoTenantSpecifiedException(token)
    }
  }

  private def toTenant(keystoneTenant: KeystoneTenant): Tenant = {
    Tenant(keystoneTenant.getId,
      keystoneTenant.getName,
      keystoneTenant.getDescription,
      Try(Option(keystoneTenant.isEnabled)).getOrElse(None))
  }

  private def toToken(tenant: Tenant, keystoneToken: KeystoneToken): Token = {
    Token(keystoneToken.getId, Option(tenant))
  }

  private def toUser(keystoneUser: KeystoneUser): User = {
    User(keystoneUser.getId,
      keystoneUser.getName,
      Option(keystoneUser.getEmail),
      Try(Option(keystoneUser.isEnabled)).getOrElse(None),
      Option(keystoneUser.getTenantId))
  }
}

case class KeystoneUserContext(
    token: Token,
    tenant: Tenant,
    user: User,
    roles: Set[Role])
  extends UserContext {
  require(token.tenant.isDefined)
  override val tenantId: String = token.tenant.get.id
}
