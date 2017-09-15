/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.commons.spark.sql

import org.apache.spark.sql.UDFRegistration

/**
 * Holds user defined functions that can be injected to UDFRegistration
 * All the functions have to operate on java.lang.Double as input and output,
 * scala.Double does not support null values (null is converted to 0.0)
 * which would lead to undesired information loss (we expect null values in DataFrames)
 */
object UserDefinedFunctions extends Serializable {

  /**
   * Registers user defined function in given UDFRegistration
   */
  def registerFunctions(udf: UDFRegistration): Unit = {
    udf.register("ABS", abs _)
    udf.register("EXP", exp _)
    udf.register("POW", power _)
    udf.register("SQRT", sqrt _)
    udf.register("SIN", sin _)
    udf.register("COS", cos _)
    udf.register("TAN", tan _)
    udf.register("LN", ln _)
    udf.register("MINIMUM", min _)
    udf.register("MAXIMUM", max _)
  }

  def abs(d: java.lang.Double): java.lang.Double = {
    if (d == null) {
      null
    } else {
      math.abs(d)
    }
  }

  def exp(d: java.lang.Double): java.lang.Double = {
    if (d == null) {
      null
    } else {
      math.exp(d)
    }
  }

  def power(d1: java.lang.Double, d2: java.lang.Double): java.lang.Double = {
    if (d1 == null || d2 == null) {
      null
    } else {
      math.pow(d1, d2)
    }
  }

  def sqrt(d: java.lang.Double): java.lang.Double = {
    if (d == null) {
      null
    } else {
      math.sqrt(d)
    }
  }

  def sin(d: java.lang.Double): java.lang.Double = {
    if (d == null) {
      null
    } else {
      math.sin(d)
    }
  }

  def cos(d: java.lang.Double): java.lang.Double = {
    if (d == null) {
      null
    } else {
      math.cos(d)
    }
  }

  def tan(d: java.lang.Double): java.lang.Double = {
    if (d == null) {
      null
    } else {
      math.tan(d)
    }
  }

  def ln(d: java.lang.Double): java.lang.Double = {
    if (d == null) {
      null
    } else {
      math.log(d)
    }
  }

  def min(d1: java.lang.Double, d2: java.lang.Double): java.lang.Double = {
    if (d1 == null || d2 == null) {
      null
    } else {
      math.min(d1, d2)
    }
  }

  def max(d1: java.lang.Double, d2: java.lang.Double): java.lang.Double = {
    if (d1 == null || d2 == null) {
      null
    } else {
      math.max(d1, d2)
    }
  }
}
