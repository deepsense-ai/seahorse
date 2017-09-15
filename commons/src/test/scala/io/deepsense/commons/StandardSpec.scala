/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

/**
 * Standard base class for tests.  Includes the following features:
 *
 *   - WordSpec style tests with Matcher DSL for assertions
 *
 *   - Support for testing Futures including the useful whenReady construct
 */
class StandardSpec
  extends WordSpec
  with Matchers
  with ScalaFutures {
}

