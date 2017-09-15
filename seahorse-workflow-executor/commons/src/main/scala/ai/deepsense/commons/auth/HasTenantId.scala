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

package ai.deepsense.commons.auth

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
