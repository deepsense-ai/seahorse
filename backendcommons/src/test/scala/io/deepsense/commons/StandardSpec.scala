/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons

import scala.concurrent.duration._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import spray.testkit.ScalatestRouteTest

/**
 * Standard base class for tests.  Includes the following features:
 *
 *   - WordSpec style tests with Matcher DSL for assertions
 *
 *   - Support for testing Futures including the useful whenReady construct
 *
 *   - Support for testing spray Routes
 */
class StandardSpec
  extends WordSpec
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest {
  protected implicit def routeTestTimeout: StandardSpec.this.type#RouteTestTimeout = {
    RouteTestTimeout(1.second)
  }
}

