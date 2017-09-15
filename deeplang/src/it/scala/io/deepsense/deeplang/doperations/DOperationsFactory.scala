/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.deeplang.doperations

trait DOperationsFactory {

  def createWriteDataFrameOperation(name: String, description: String): WriteDataFrame = {
    val operation = new WriteDataFrame
    val nameParameter = operation.parameters.getStringParameter(WriteDataFrame.nameParam)
    nameParameter.value = Some(name)
    val descriptionParameter =
      operation.parameters.getStringParameter(WriteDataFrame.descriptionParam)
    descriptionParameter.value = Some(description)
    operation
  }

  def createReadDataFrameOperation(entityId: String): ReadDataFrame = {
    val readDataFrameOperation = new ReadDataFrame
    val idParameter = readDataFrameOperation.parameters.getStringParameter(ReadDataFrame.idParam)
    idParameter.value = Some(entityId)
    readDataFrameOperation
  }
}

object DOperationsFactory extends DOperationsFactory