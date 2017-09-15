/**
 * Copyright (c) 2015, CodiLime Inc.
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
      featureColumns: Option[MultipleColumnSelection] = None,
      targetColumn: Option[SingleColumnSelection] = None) {

    def featureColumnNames(dataframe: DataFrame): Seq[String] =
      dataframe.getColumnNames(featureColumns.get)

    def targetColumnName(dataframe: DataFrame): String = dataframe.getColumnName(targetColumn.get)

    /**
     * Names of columns w.r.t. certain dataframe.
     * @param dataframe DataFrame that we want to use.
     * @return A tuple in form (sequence of feature column names, target column name)
     */
    def columnNames(dataframe: DataFrame): (Seq[String], String) =
      (featureColumnNames(dataframe), targetColumnName(dataframe))
  }
}
