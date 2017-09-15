/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.modeldeploying

import java.util.UUID

import scala.collection.mutable.Map

class ModelRepository(models: Map[UUID, Model] = Map()) {
  def put(model: Model): UUID = {
    val id = UUID.randomUUID()
    models += id -> model
    id
  }

  def get(id: UUID): Model = {
    models(id)
  }
}
