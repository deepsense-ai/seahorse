/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.examples

import ai.deepsense.deeplang.doperations.SqlCombine

class SqlCombineExample extends AbstractOperationExample[SqlCombine] {
  override def dOperation: SqlCombine = {
    new SqlCombine()
      .setLeftTableName("beds")
      .setRightTableName("prices")
      .setSqlCombineExpression(
        """
          |SELECT DISTINCT beds.city, beds.beds
          |FROM beds
          |JOIN prices ON beds.city = prices.city
          |AND prices.price < 120000 * beds.beds
        """.stripMargin)
  }

  override def fileNames: Seq[String] = Seq("example_city_beds", "example_city_price")
}
