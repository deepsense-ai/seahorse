/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.auth.usercontext

import io.deepsense.commons.auth.HasTenantId

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
