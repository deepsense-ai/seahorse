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

package ai.deepsense.commons.types

/**
 * Types of data that column in dataframe can have.
 */
object ColumnType extends Enumeration {
  type ColumnType = Value
  val numeric = Value("numeric")
  val boolean = Value("boolean")
  val string = Value("string")
  val timestamp = Value("timestamp")
  val array = Value("array")
  val vector = Value("vector")
  val other = Value("other")
}
