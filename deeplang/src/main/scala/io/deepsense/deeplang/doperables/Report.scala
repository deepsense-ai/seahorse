/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.DOperable

case class Report(message: String = "") extends DOperable {
  override def report: Report = this
}
