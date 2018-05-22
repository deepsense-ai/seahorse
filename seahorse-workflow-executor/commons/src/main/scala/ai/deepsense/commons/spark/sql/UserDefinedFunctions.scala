/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.spark.sql

import java.lang.{Double => JavaDouble}

import ai.deepsense.sparkutils.spi.SparkSessionInitializer
import org.apache.spark.sql.{SparkSession, UDFRegistration}

class UserDefinedFunctions() extends SparkSessionInitializer {
  override def init(sparkSession: SparkSession): Unit =
    UserDefinedFunctions.registerFunctions(sparkSession.udf)
}
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
  private def registerFunctions(udf: UDFRegistration): Unit = {
    udf.register("ABS", nullSafeSingleParamOp(math.abs))
    udf.register("EXP", nullSafeSingleParamOp(math.exp))
    udf.register("POW", nullSafeTwoParamOp(math.pow))
    udf.register("SQRT", nullSafeSingleParamOp(math.sqrt))
    udf.register("SIN", nullSafeSingleParamOp(math.sin))
    udf.register("COS", nullSafeSingleParamOp(math.cos))
    udf.register("TAN", nullSafeSingleParamOp(math.tan))
    udf.register("LN", nullSafeSingleParamOp(math.log))
    udf.register("MINIMUM", nullSafeTwoParamOp(math.min))
    udf.register("MAXIMUM", nullSafeTwoParamOp(math.max))
    udf.register("FLOOR", nullSafeSingleParamOp(math.floor))
    udf.register("CEIL", nullSafeSingleParamOp(math.ceil))
    udf.register("SIGNUM", nullSafeSingleParamOp(math.signum))
  }


  private def nullSafeTwoParamOp(
           f: (Double, Double) => Double): (JavaDouble, JavaDouble) => java.lang.Double = {
    case (d1, d2) =>
      if (d1 == null || d2 == null) {
        null
      } else {
        f(d1, d2)
      }
  }

  private def nullSafeSingleParamOp(f: (Double) => Double): (JavaDouble) => JavaDouble = {
    case (d) =>
      if (d == null) {
        null
      } else {
        f(d)
      }
  }
}
