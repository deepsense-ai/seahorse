/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service.db.dbio

import java.util.UUID

import slick.dbio.Effect.{Read, Write}
import slick.driver.JdbcProfile

import io.deepsense.commons.service.api.CommonApiExceptions

abstract class GenericDBIOs[Api, Db <: {def id : UUID}] {

  import scala.concurrent.ExecutionContext.Implicits.global

  val api: JdbcProfile#API
  val table: api.TableQuery[_ <: api.Table[Db] {
    def id: api.Rep[UUID]
  }]

  import api._

  val fromDB: Db => Api
  val fromApi: Api => Db

  def get(id: UUID): api.DBIOAction[Api, NoStream, Read] = for {
    entityOpt <- table.filter(_.id === id).result.headOption
    entity <- checkExists(id, entityOpt)
  } yield fromDB(entity)

  def getAll: DBIOAction[List[Api], NoStream, Read] = for {
    entities <- table.result
  } yield entities.map(fromDB).toList

  def insertOrUpdate(id: UUID, apiEntity: Api): DBIOAction[Api, NoStream, Write with Read] = {
    val entity = fromApi(apiEntity)
    for {
      _ <- pathParamsMustMatchBodyParams(id, entity)
      insertedCount <- table.insertOrUpdate(entity)
      justInserted <- table.filter(_.id === id).result.head
    } yield fromDB(justInserted)
  }

  def delete(id: UUID): DBIOAction[Unit, NoStream, Write] = for {
    _ <- table.filter(_.id === id).delete
  } yield ()

  private def checkExists(id: UUID, option: Option[Db]): DBIOAction[Db, NoStream, Effect] = option match {
    case Some(value) => DBIO.successful(value)
    case None => DBIO.failed(CommonApiExceptions.doesNotExist(id))
  }

  private def pathParamsMustMatchBodyParams(id: UUID, entity: Db) = {
    if (entity.id == id) {
      DBIO.successful(())
    } else {
      DBIO.failed(CommonApiExceptions.pathIdMustMatchBodyId(id, entity.id))
    }
  }

}
