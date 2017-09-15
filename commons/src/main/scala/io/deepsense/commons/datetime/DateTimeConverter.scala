/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.commons.datetime

import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, DateTimeZone}

object DateTimeConverter {

  val zone: DateTimeZone =  DateTimeZone.UTC
  val dateTimeFormatter: DateTimeFormatter = ISODateTimeFormat.dateTime()
  def convertToString(dateTime: DateTime) = dateTime.toString(dateTimeFormatter)
  def fromString(dateTimeString: String) =
    dateTimeFormatter.parseDateTime(dateTimeString).withZone(zone)
  def now = new DateTime(zone)
}
