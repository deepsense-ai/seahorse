/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graph

case class Progress(current: Int, total: Int) {
  require(current >= 0 && current <= total)
}
