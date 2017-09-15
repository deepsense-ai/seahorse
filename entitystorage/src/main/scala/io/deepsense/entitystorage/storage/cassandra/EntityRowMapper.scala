/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.storage.cassandra

import com.datastax.driver.core.Row

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.models.entities.{DataObjectReference, DataObjectReport, Entity}

object EntityRowMapper {

  val Id = "id"
  val TenantId = "tenantid"
  val Name = "name"
  val Description = "description"
  val DClass = "dclass"
  val Created = "created"
  val Updated = "updated"
  val Data = "data"
  val Url = "url"
  val Saved = "saved"
  val Report = "report"

  def fromRow(row: Row): Entity =
    Entity(
      row.getString(TenantId),
      row.getUUID(Id),
      row.getString(Name),
      row.getString(Description),
      row.getString(DClass),
      readData(row),
      readReport(row),
      DateTimeConverter.fromMillis(row.getDate(Created).getTime),
      DateTimeConverter.fromMillis(row.getDate(Updated).getTime),
      row.getBool(Saved)
    )

  def readData(row: Row): Option[DataObjectReference] =
    Option(row.getString(Url)).map(DataObjectReference)

  def readReport(row: Row): Option[DataObjectReport] =
    Option(row.getString(Report)).map(DataObjectReport)
}
