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

package io.deepsense.commons.datetime

import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, DateTimeZone}

trait DateTimeConverter {
  val zone: DateTimeZone = DateTimeZone.UTC
  val dateTimeFormatter: DateTimeFormatter = ISODateTimeFormat.dateTime()
  def toString(dateTime: DateTime): String = dateTime.toString(dateTimeFormatter)
  def parseDateTime(s: String): DateTime = dateTimeFormatter.parseDateTime(s).withZone(zone)
  def now: DateTime = new DateTime(zone)
  def fromMillis(millis: Long): DateTime = new DateTime(zone).withMillis(millis)
}

object DateTimeConverter extends DateTimeConverter
