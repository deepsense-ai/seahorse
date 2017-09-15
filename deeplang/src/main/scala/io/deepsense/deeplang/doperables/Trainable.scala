/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.{DMethod1To1, DOperable, ExecutionContext}
import io.deepsense.entitystorage.UniqueFilenameUtil

trait Trainable extends DOperable {
  val train: DMethod1To1[Trainable.Parameters, DataFrame, Scorable]

  protected def saveScorable(context: ExecutionContext, scorable: Scorable): Unit = {
    val uniquePath = context.uniqueHdfsFileName(UniqueFilenameUtil.ModelEntityCategory)
    scorable.save(context)(uniquePath)
  }
}

object Trainable {
  case class Parameters(
    featureColumns: MultipleColumnSelection,
    targetColumn: SingleColumnSelection)
}
