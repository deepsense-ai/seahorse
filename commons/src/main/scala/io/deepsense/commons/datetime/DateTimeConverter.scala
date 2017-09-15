/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.commons.datetime

import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, DateTimeZone}

trait DateTimeConverter {
  val zone: DateTimeZone =  DateTimeZone.UTC
  val dateTimeFormatter: DateTimeFormatter = ISODateTimeFormat.dateTime()
  def toString(dateTime: DateTime) = dateTime.toString(dateTimeFormatter)
  def parseDateTime(s: String) = dateTimeFormatter.parseDateTime(s).withZone(zone)
  def now = new DateTime(zone)
  def fromMillis(millis: Long) = new DateTime(zone).withMillis(millis)
}

object DateTimeConverter extends DateTimeConverter
