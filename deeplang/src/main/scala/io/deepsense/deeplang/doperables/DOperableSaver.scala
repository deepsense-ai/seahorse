/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import scala.concurrent.Await

import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.models.entities.{Entity, InputEntity}

trait DOperableSaver {

  def saveDOperableWithEntityStorageRegistration(
      context: ExecutionContext)(
      dOperable: DOperable,
      inputEntity: InputEntity): Entity = {
    val uniqueFilename: String = inputEntity.data.get.url
    dOperable.save(context)(uniqueFilename)
    saveEntity(context, uniqueFilename, inputEntity)
  }

  private def saveEntity(
      context: ExecutionContext,
      uniqueFilename: String,
      inputEntity: InputEntity): Entity = {
    import scala.concurrent.duration._
    // TODO: duration from configuration (and possibly a little longer timeout)
    implicit val timeout = 5.seconds
    val entityF = context.entityStorageClient.createEntity(inputEntity)
    // TODO: be sure that this will fail if timeout expired
    Await.result(entityF, timeout)
  }
}

object DOperableSaver extends DOperableSaver
