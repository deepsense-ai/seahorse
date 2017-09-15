/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.commons.auth

import io.deepsense.commons.auth.exceptions.ResourceAccessDeniedException
import io.deepsense.commons.auth.usercontext.UserContext

/**
 * Describes an object with an owner.
 */
trait Ownable extends HasTenantId {

  /**
   * Checks if this object is owned by user defined in UserContext.
   * @param owner Owner's UserContext.
   * @return The object when it is owned by the user defined in the UserContext. Otherwise throws
   *         [[io.deepsense.commons.auth.exceptions.ResourceAccessDeniedException ResourceAccessDeniedException]].
   */
  def assureOwnedBy(owner: UserContext): this.type = {
    if (isOwnedBy(owner)) {
      this
    } else {
      throw new ResourceAccessDeniedException(owner, this)
    }
  }

  /**
   * Checks if this object is owned by user defined in UserContext.
   * @param owner Owner's UserContext.
   * @return True if the object is owned by the user defined in the UserContext.
   *         Otherwise false.
   */
  def isOwnedBy(owner: UserContext): Boolean = this.tenantMatches(owner)
}
