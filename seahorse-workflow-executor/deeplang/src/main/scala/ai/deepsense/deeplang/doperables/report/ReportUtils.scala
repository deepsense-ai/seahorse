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

package ai.deepsense.deeplang.doperables.report

import ai.deepsense.deeplang.utils.SparkTypeConverter

object ReportUtils {

  val StringPreviewMaxLength = 300

  def shortenLongStrings(value: String, maxLength: Int = StringPreviewMaxLength): String =
    if (value.length < maxLength) {
      value
    } else {
      value.take(maxLength) + "..."
    }

  def shortenLongTableValues(
    vals: List[List[Option[String]]],
    maxLength: Int = StringPreviewMaxLength): List[List[Option[String]]] =
    vals.map(_.map {
      case None => None
      case Some(strVal) => Some(ReportUtils.shortenLongStrings(strVal, maxLength))
    }
  )

  def formatCell(cell: Any): String =
    SparkTypeConverter.sparkAnyToString(cell)

  def formatValues(values: List[List[Option[Any]]]): List[List[Option[String]]] =
    values.map(_.map(_.map(formatCell)))
}
