/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.deeplang.doperations

trait DOperationsFactory {

  def createSaveDataFrameOperation(name: String, description: String): SaveDataFrame = {
    val operation = new SaveDataFrame
    val nameParameter = operation.parameters.getStringParameter(SaveDataFrame.nameParam)
    nameParameter.value = Some(name)
    val descriptionParameter =
      operation.parameters.getStringParameter(SaveDataFrame.descriptionParam)
    descriptionParameter.value = Some(description)
    operation
  }

  def createLoadDataFrameOperation(entityId: String): LoadDataFrame = {
    val loadDataFrameOperation = new LoadDataFrame
    val idParameter = loadDataFrameOperation.parameters.getStringParameter(LoadDataFrame.idParam)
    idParameter.value = Some(entityId)
    loadDataFrameOperation
  }
}

object DOperationsFactory extends DOperationsFactory
