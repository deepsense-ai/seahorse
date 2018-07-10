/**
  * Copyright 2018 deepsense.ai (CodiLime, Inc)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package ai.deepsense.seahorse.datasource.server

import java.util.UUID

import org.scalatest.{FreeSpec, Matchers}
import ai.deepsense.seahorse.datasource.model.Visibility
import ai.deepsense.seahorse.datasource.converters.RichDatasourceParams._
import ai.deepsense.seahorse.datasource.converters.SparkOptionDbFromApi
import ai.deepsense.seahorse.datasource.model.SparkGenericOptions


class RichDatasourceApiSpec extends FreeSpec with Matchers {
  "Rich datasource Api" - {
    "can convert DB SparkOptions to API" in {
      val sparkGenericDatasource = TestData.someSparkGeneralFormatOption(visibility = Some(Visibility.publicVisibility))
      val sparkOption = sparkGenericDatasource.getSparkOptions()
      sparkOption.isSuccess shouldBe true
      val sparkOptionList = sparkOption.getOrElse(List())
      sparkOptionList.size shouldBe 2
    }
  }

}
