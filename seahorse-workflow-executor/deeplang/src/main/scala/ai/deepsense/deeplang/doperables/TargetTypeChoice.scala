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

package ai.deepsense.deeplang.doperables

import org.apache.spark.sql.types._

import ai.deepsense.deeplang.params.choice.Choice

sealed abstract class TargetTypeChoice(val columnType: DataType) extends Choice {
  override val choiceOrder: List[Class[_ <: Choice]] = TargetTypeChoices.choiceOrder

  override val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
  val name = columnType.simpleString
}

object TargetTypeChoices {
  val choiceOrder = List(
    StringTargetTypeChoice(),
    BooleanTargetTypeChoice(),
    TimestampTargetTypeChoice(),
    DoubleTargetTypeChoice(),
    FloatTargetTypeChoice(),
    LongTargetTypeChoice(),
    IntegerTargetTypeChoice()).map(_.getClass)

  case class StringTargetTypeChoice() extends TargetTypeChoice(StringType)
  case class DoubleTargetTypeChoice() extends TargetTypeChoice(DoubleType)
  case class TimestampTargetTypeChoice() extends TargetTypeChoice(TimestampType)
  case class BooleanTargetTypeChoice() extends TargetTypeChoice(BooleanType)
  case class IntegerTargetTypeChoice() extends TargetTypeChoice(IntegerType)
  case class FloatTargetTypeChoice() extends TargetTypeChoice(FloatType)
  case class LongTargetTypeChoice() extends TargetTypeChoice(LongType)
}
