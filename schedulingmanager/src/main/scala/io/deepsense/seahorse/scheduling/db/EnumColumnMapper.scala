/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.db

import slick.jdbc.JdbcType

import io.deepsense.seahorse.scheduling.db.Database

object EnumColumnMapper {

  import Database.driver.api._

  def apply[E <: Enumeration](enum: E): JdbcType[E#Value] = MappedColumnType.base[E#Value, String](
    e => e.toString,
    s => enum.withName(s)
  )
}
