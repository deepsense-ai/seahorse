/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import spray.json._

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.{DMethod1To1, DOperable, ExecutionContext}
import io.deepsense.entitystorage.UniqueFilenameUtil
import io.deepsense.models.entities.{DataObjectReference, DataObjectReport, InputEntity}
import io.deepsense.reportlib.model.ReportJsonProtocol

trait Trainable extends DOperable {
  val train: DMethod1To1[Trainable.Parameters, DataFrame, Scorable]

  protected def saveScorable(context: ExecutionContext, scorable: Scorable) = {
    val uniquePath = UniqueFilenameUtil.getUniqueHdfsFilename(
      context.tenantId,
      UniqueFilenameUtil.ModelEntityCategory)
    import ReportJsonProtocol._
    DOperableSaver.saveDOperableWithEntityStorageRegistration(context)(
      scorable,
      inputEntity(
        context.tenantId,
        scorable,
        uniquePath,
        scorable.report.content.toJson.prettyPrint))
  }

  private def inputEntity(
      tenantId: String,
      scorable: Scorable,
      uniquePath: String,
      jsonReport: String): InputEntity =
    InputEntity(
      tenantId,
      scorable.getClass.getSimpleName,
      "Trained Model",
      scorable.getClass.getCanonicalName,
      Some(DataObjectReference(uniquePath)),
      Some(DataObjectReport(jsonReport)),
      saved = true)
}

object Trainable {
  case class Parameters(
    featureColumns: MultipleColumnSelection,
    targetColumn: SingleColumnSelection)
}
