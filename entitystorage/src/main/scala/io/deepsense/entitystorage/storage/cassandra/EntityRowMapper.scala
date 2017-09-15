/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.entitystorage.storage.cassandra

import com.datastax.driver.core.Row
import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.doperables.Report
import io.deepsense.entitystorage.models.{DataObject, DataObjectReference, DataObjectReport, Entity}

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

  def fromRow(row: Row): Entity =
    Entity(
      row.getString(TenantId),
      row.getUUID(Id),
      row.getString(Name),
      row.getString(Description),
      row.getString(DClass),
      DateTimeConverter.fromMillis(row.getDate(Created).getTime),
      DateTimeConverter.fromMillis(row.getDate(Updated).getTime),
      readDataObject(row),
      row.getBool(Saved)
    )

  def readDataObject(row: Row): DataObject = {
    if (row.isNull(Url)) {
      // TODO Read report from DB
      // val report = row.getUDTValue(Data)
      DataObjectReport(Report())
    } else {
      DataObjectReference(row.getString(Url))
    }
  }

  def toDataOrUrl(entity: Entity): Either[String, Report] = {
    entity.data match {
      case DataObjectReference(url) => Left(url)
      case DataObjectReport(report) => Right(report)
    }
  }
}

