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

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration._

import com.google.common.base.Optional
import org.jclouds.openstack.keystone.v2_0.domain.{Role => KeystoneRole, Tenant => KeystoneTenant, Token => KeystoneToken, User => KeystoneUser}
import org.jclouds.openstack.keystone.v2_0.features.TokenApi
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import ai.deepsense.commons.{StandardSpec, UnitTestSupport}

class KeystoneTokenTranslatorSpec extends StandardSpec with UnitTestSupport {
  val tenantId = "tenantId"
  val tenantName = "tenantname"
  val tenantDescription = "Description"
  val tenantEnable = Some(true)
  val tenant = keystoneTenantMock(tenantId, tenantName, tenantDescription, tenantEnable.get)
  val expectedTenant = Tenant(tenantId, tenantName, tenantDescription, tenantEnable)

  val tokenId = "id"
  val tenantOptional = Optional.of(tenant)
  val token = keystoneTokenMock(tokenId, tenantOptional)
  val tokenNoTenant = keystoneTokenMock(tokenId, Optional.absent())

  val userId = "userId"
  val userName = "userName"
  val userEmail = Some("email")
  val userEnabled = Some(true)
  val userTenantId = Some("fakeTenantId")
  val userRoles = Set[KeystoneRole]()
  val user = keystoneUserMock(
    userId, userName, userEmail.get,
    userEnabled.get, userTenantId.get, userRoles)
  val tokenApi = mock[TokenApi]

  val keystoneTokenTranslator = new KeystoneTokenTranslator(tokenApi)

  "KeystoneTokenTranslator" should {
    "return UserContext" when {
      "everything is OK" in {
        when(tokenApi.get(any(classOf[String]))).thenReturn(token)
        when(tokenApi.getUserOfToken(any(classOf[String]))).thenReturn(user)
        val userContextFuture = keystoneTokenTranslator.translate("this can be any")
        val userContext = Await.result(userContextFuture, 2.seconds)
        userContext.roles shouldBe empty

        userContext.token should have(
          'id (tokenId),
          'tenant (Option(expectedTenant))
        )

        userContext.tenant should have(
          'id (tenantId),
          'name (tenantName),
          'description (tenantDescription),
          'enabled (tenantEnable)
        )

        userContext.user should have(
          'id (userId),
          'name (userName),
          'email (userEmail),
          'enabled (userEnabled),
          'tenantId (userTenantId)
        )
      }
    }
    "return Failure" when {
      "tokenApi.get returns null" in {
        when(tokenApi.get(any(classOf[String]))).thenReturn(null)
        when(tokenApi.getUserOfToken(any(classOf[String]))).thenReturn(user)
        val userContextFuture = keystoneTokenTranslator.translate("this can be any1")
        ScalaFutures.whenReady(userContextFuture.failed) { e =>
          e shouldBe a [CannotGetTokenException]
          e.asInstanceOf[InvalidTokenException]
        }
      }
      "tokenApi.getUserOfToken returns null" in {
        when(tokenApi.get(any(classOf[String]))).thenReturn(token)
        when(tokenApi.getUserOfToken(any(classOf[String]))).thenReturn(null)
        val userContextFuture = keystoneTokenTranslator.translate("this can be any2")
        ScalaFutures.whenReady(userContextFuture.failed) { e =>
          e shouldBe a [CannotGetUserException]
        }
      }
      "no tenant is specified for the token" in {
        when(tokenApi.get(any(classOf[String]))).thenReturn(tokenNoTenant)
        when(tokenApi.getUserOfToken(any(classOf[String]))).thenReturn(user)
        val userContextFuture = keystoneTokenTranslator.translate("this can be any3")
        ScalaFutures.whenReady(userContextFuture.failed) { e =>
          e shouldBe a [NoTenantSpecifiedException]
        }
      }
    }
  }

  private def keystoneTenantMock(
      id: String,
      name: String,
      description: String,
      enabled: Boolean): KeystoneTenant = {
    val tenant = mock[KeystoneTenant]
    when(tenant.getDescription).thenReturn(description)
    when(tenant.getId).thenReturn(id)
    when(tenant.isEnabled).thenReturn(enabled)
    when(tenant.getName).thenReturn(name)
    tenant
  }

  private def keystoneTokenMock(
      id: String,
      tenantOptional: Optional[KeystoneTenant]): KeystoneToken = {
    val token = mock[KeystoneToken]
    when(token.getId).thenReturn(id)
    when(token.getTenant).thenReturn(tenantOptional)
    token
  }

  private def keystoneUserMock(
      id: String,
      name: String,
      email: String,
      enabled: Boolean,
      tenantId: String,
      roles: Set[KeystoneRole]): KeystoneUser = {
    val builder = KeystoneUser.builder()
    builder.id(id)
    builder.name(name)
    builder.email(email)
    builder.enabled(enabled)
    builder.tenantId(tenantId)
    builder.roles(roles)
    builder.build()
  }
}
