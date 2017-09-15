/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.utils

import scala.concurrent.Future

object OptionOpts {

  implicit class OptionOpts[T](valueOpt: Option[T]) {
    def asFuture = valueOpt match {
      case Some(value) => Future.successful(value)
      case None => Future.failed(new IllegalStateException())
    }
  }

}
