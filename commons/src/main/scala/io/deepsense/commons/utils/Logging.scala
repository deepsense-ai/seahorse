/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.utils

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait Logging {
  @transient
  protected lazy val logger: Logger =
    Logger(LoggerFactory.getLogger(getClass.getName))
}
