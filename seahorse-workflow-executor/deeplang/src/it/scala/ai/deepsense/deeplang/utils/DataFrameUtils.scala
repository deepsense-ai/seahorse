/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.utils

import ai.deepsense.deeplang.doperables.dataframe.DataFrame

object DataFrameUtils {

  def collectValues(dataFrame: DataFrame, columnName: String): Set[Any] = {
    dataFrame.sparkDataFrame.select(columnName).rdd.map(_.get(0)).collect().toSet
  }
}
