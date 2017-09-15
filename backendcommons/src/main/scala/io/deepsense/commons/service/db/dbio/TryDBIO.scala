/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service.db.dbio

import slick.dbio._

import scala.util.{Failure, Success, Try}

object TryDBIO {
  def apply[T](action: => T): DBIOAction[T, NoStream, Effect] = {
    Try {
      action
    } match {
      case Success(value) => DBIO.successful(value)
      case Failure(x) => DBIO.failed(x)
    }
  }
}
