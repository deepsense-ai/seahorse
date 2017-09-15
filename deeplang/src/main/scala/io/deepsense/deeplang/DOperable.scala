/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang

import io.deepsense.deeplang.doperables.Report

/**
 * Represents objects on which you can perform DOperations.
 */
trait DOperable {

  def report: Report

  /**
   * Saves DOperable on HDFS under specified path.
   * Sets url so that it informs where it has been saved.
   */
  def save(executionContext: ExecutionContext)(path: String): Unit

  /**
   * @return path where DOperable is stored, if None is returned then DOperable is not persisted.
   */
  def url: Option[String] = None
}
