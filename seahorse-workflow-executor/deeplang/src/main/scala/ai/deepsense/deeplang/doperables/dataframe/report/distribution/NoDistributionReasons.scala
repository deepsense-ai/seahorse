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

package ai.deepsense.deeplang.doperables.dataframe.report.distribution

import org.apache.spark.sql.types.DataType

object NoDistributionReasons {

  val TooManyDistinctCategoricalValues = "Too many distinct categorical values"

  val NoData = "No data to calculate distribution"

  val OnlyNulls = "No data to calculate distribution - only nulls"

  val SimplifiedReport = "No distributions for simplified report"

  def NotApplicableForType(dataType: DataType): String =
    s"Distribution not applicable for type ${dataType.typeName}"
}
