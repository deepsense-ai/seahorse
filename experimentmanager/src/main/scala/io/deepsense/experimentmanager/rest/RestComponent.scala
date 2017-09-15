/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest

import spray.routing.Route

/**
 * RestComponent allows to expose a REST API.
 */
trait RestComponent {
  def route: Route
}
