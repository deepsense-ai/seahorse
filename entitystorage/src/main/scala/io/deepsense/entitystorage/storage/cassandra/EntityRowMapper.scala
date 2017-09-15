/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.storage.cassandra

import com.datastax.driver.core.Row

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.models.entities._

object EntityRowMapper {

  val Id = "id"
  val TenantId = "tenantid"
  val Name = "name"
  val Description = "description"
  val DClass = "dclass"
  val Created = "created"
  val Updated = "updated"
  val Url = "url"
  val Saved = "saved"
  val Report = "report"

  lazy val EntityInfoFields: Seq[String] = Seq(
    Id, TenantId, Name, Description, DClass, Created, Updated, Saved)

  lazy val EntityWithDataFields: Seq[String] = EntityInfoFields :+ Url

  lazy val EntityWithReportFields: Seq[String] = EntityInfoFields :+ Report

  def toEntityInfo(row: Row): EntityInfo = EntityInfo(
    entityId = Entity.Id(row.getUUID(Id)),
    tenantId = row.getString(TenantId),
    name = row.getString(Name),
    description = row.getString(Description),
    dClass = row.getString(DClass),
    created = DateTimeConverter.fromMillis(row.getDate(Created).getTime),
    updated = DateTimeConverter.fromMillis(row.getDate(Updated).getTime),
    saved = row.getBool(Saved))

  def toEntityWithData(row: Row): EntityWithData = EntityWithData(
    info = toEntityInfo(row),
    dataReference = DataObjectReference(row.getString(Url))
  )

  def toEntityWithReport(row: Row): EntityWithReport = EntityWithReport(
    info = toEntityInfo(row),
    report = DataObjectReport(row.getString(Report))
  )
}
