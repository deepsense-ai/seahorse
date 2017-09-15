/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import scala.concurrent.Await

import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.models.entities.{CreateEntityRequest, Entity}
trait DOperableSaver {

  def saveDOperableWithEntityStorageRegistration(
      context: ExecutionContext)(
      dOperable: DOperable,
      entity: CreateEntityRequest): Entity.Id = {
    val uniqueFilename: String = entity.dataReference.get.savedDataPath
    dOperable.save(context)(uniqueFilename)
    saveEntity(context, uniqueFilename, entity)
  }

  private def saveEntity(
      context: ExecutionContext,
      uniqueFilename: String,
      inputEntity: CreateEntityRequest): Entity.Id = {
    import scala.concurrent.duration._
    // TODO: duration from configuration (and possibly a little longer timeout)
    implicit val timeout = 5.seconds
    val future = context.entityStorageClient.createEntity(inputEntity)
    // TODO: be sure that this will fail if timeout expired
    Await.result(future, timeout)
  }
}

object DOperableSaver extends DOperableSaver
