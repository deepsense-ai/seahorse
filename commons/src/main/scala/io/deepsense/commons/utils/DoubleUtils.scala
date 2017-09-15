/*
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.utils

import java.math.RoundingMode
import java.text.{DecimalFormatSymbols, DecimalFormat}
import java.util.Locale

object DoubleUtils {

  def double2String(d: Double): String = {
    val formatter: DecimalFormat =
      new DecimalFormat("#.######", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.setRoundingMode(RoundingMode.HALF_UP)
    formatter.format(d)
  }
}
