/**
 * Copyright (c) 2015, CodiLime Inc.
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
