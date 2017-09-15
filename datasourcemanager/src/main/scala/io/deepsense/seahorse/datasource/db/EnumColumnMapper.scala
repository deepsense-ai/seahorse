/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db

import slick.jdbc.JdbcType

object EnumColumnMapper {

  import Database.driver.api._

  def apply[E <: Enumeration](enum: E): JdbcType[E#Value] = MappedColumnType.base[E#Value, String](
    e => e.toString,
    s => enum.withName(s)
  )
}
