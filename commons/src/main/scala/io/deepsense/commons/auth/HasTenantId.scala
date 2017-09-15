/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.auth

/**
 * States that the object has a tenant Id assigned to it.
 * Note that one of these objects can be a tenant itself.
 */
trait HasTenantId {
  val tenantId: String

  /**
   * Returns true if this and that belong to the same tenant.
   * @param that The other object that has tenant Id.
   * @return True if this and that belong to the same tenant. Otherwise false.
   */
  def tenantMatches(that: HasTenantId): Boolean = this.tenantId == that.tenantId
}
