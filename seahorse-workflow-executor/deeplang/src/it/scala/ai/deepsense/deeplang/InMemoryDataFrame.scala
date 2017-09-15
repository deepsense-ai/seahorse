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

package ai.deepsense.deeplang

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

trait InMemoryDataFrame { self: BeforeAndAfter with BeforeAndAfterAll with LocalExecutionContext =>

  lazy val inMemoryDataFrame = createDataFrame(rows, schema)

  private lazy val schema: StructType =
    StructType(Seq(
      StructField("boolean", BooleanType),
      StructField("double", DoubleType),
      StructField("string", StringType)
    ))

  private lazy val rows = {
    val base = Seq(
      Row(true, 0.45, "3.14"),
      Row(false, 0.2, "\"testing...\""),
      Row(false, 3.14159, "Hello, world!"),
      Row(true, 0.1, "asd")
    )
    val repeatedFewTimes = (1 to 10).flatMap(_ => base)
    repeatedFewTimes
  }

}
