/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db

import slick.dbio.DBIO

package object dbio {

  private [dbio] implicit class ValidationOps[E <: Throwable, T](validation: scalaz.Validation[E, T]) {

    def asDBIO: DBIO[T] = validation match {
      case scalaz.Success(e) => DBIO.successful(e)
      case scalaz.Failure(ex) => DBIO.failed(ex)
    }

  }

}
