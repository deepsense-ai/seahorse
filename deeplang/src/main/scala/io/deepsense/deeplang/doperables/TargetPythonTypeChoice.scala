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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.VectorUDT
import org.apache.spark.sql.types._

import io.deepsense.deeplang.params.choice.Choice

sealed abstract class TargetPythonTypeChoice(val columnType: DataType) extends Choice {
  override val choiceOrder: List[Class[_ <: Choice]] = TargetPythonTypeChoices.choiceOrder

  override val params = declareParams()
  val name = columnType.simpleString
}

object TargetPythonTypeChoices {
  val choiceOrder = List(
    StringTargetPythonTypeChoice(),
    BooleanTargetPythonTypeChoice(),
    TimestampTargetPythonTypeChoice(),
    DoubleTargetPythonTypeChoice(),
    LongTargetPythonTypeChoice(),
    IntegerTargetPythonTypeChoice(),
    VectorUDTTargetPythonTypeChoice()).map(_.getClass)

  case class StringTargetPythonTypeChoice() extends TargetPythonTypeChoice(StringType)
  case class DoubleTargetPythonTypeChoice() extends TargetPythonTypeChoice(DoubleType)
  case class TimestampTargetPythonTypeChoice() extends TargetPythonTypeChoice(TimestampType)
  case class BooleanTargetPythonTypeChoice() extends TargetPythonTypeChoice(BooleanType)
  case class IntegerTargetPythonTypeChoice() extends TargetPythonTypeChoice(IntegerType)
  case class LongTargetPythonTypeChoice() extends TargetPythonTypeChoice(LongType)
  case class VectorUDTTargetPythonTypeChoice() extends TargetPythonTypeChoice(new VectorUDT())
}
