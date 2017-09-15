/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
