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

import ai.deepsense.commons.auth.HasTenantId

trait UserContext extends HasTenantId {
  def token: Token
  def tenant: Tenant
  def user: User
  def roles: Set[Role]

  override val tenantId: String = tenant.id
}

case class Tenant(id: String, name: String, description: String, enabled: Option[Boolean])

// TODO Jodatime?
case class Token(id: String /* , expires: Date */, tenant: Option[Tenant] = None)

case class User(
  id: String,
  name: String,
  email: Option[String],
  enabled: Option[Boolean],
  tenantId: Option[String])

case class Role(
  name: String,
  id: Option[String] = None,
  description: Option[String] = None,
  serviceId: Option[String] = None,
  tenantId: Option[String] = None)
