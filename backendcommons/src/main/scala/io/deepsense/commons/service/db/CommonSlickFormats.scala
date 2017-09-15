/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service.db

import java.sql.JDBCType
import java.util.UUID

import slick.jdbc.{PositionedParameters, SetParameter}

object CommonSlickFormats {

  implicit val uuidFormat = new SetParameter[UUID] {
    def apply(v: UUID, pp: PositionedParameters): Unit = {
      pp.setObject(v, JDBCType.BINARY.getVendorTypeNumber)
    }
  }

}
