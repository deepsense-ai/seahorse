/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.commons.utils

import java.math.{MathContext, RoundingMode}

object DoubleUtils {
  val mathContext = new MathContext(4, RoundingMode.HALF_UP)
  def double2String(d: Double): String = {
    if (d.isNaN || d.isInfinity) {
      d.toString
    } else {
      val decimal = BigDecimal(d)
        .round(mathContext)
        .toString()
      if (decimal.contains("E")) {
        decimal.replaceAll("\\.?0*E", "E")
      } else if (decimal.contains(".")) {
        decimal.replaceAll("\\.?0*$", "")
      } else {
        decimal
      }
    }
  }
}
