/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import io.deepsense.deeplang.doperables.Report

/**
 * Represents objects on which you can perform DOperations.
 */
trait DOperable {
  def report: Report
  def save(executionContext: ExecutionContext)(path: String): Unit
}
