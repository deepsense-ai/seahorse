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

package ai.deepsense.deeplang.doperations.readwritedataframe.validators

import ai.deepsense.deeplang.doperations.inout.{InputStorageTypeChoice, OutputStorageTypeChoice}
import ai.deepsense.deeplang.doperations.readwritedataframe.FilePath
import ai.deepsense.deeplang.doperations.{ReadDataFrame, WriteDataFrame}

object FilePathHasValidFileScheme {

  def validate(wdf: WriteDataFrame): Unit = {
    import OutputStorageTypeChoice._

    wdf.getStorageType() match {
      case file: File =>
        val path = file.getOutputFile()
        FilePath(path)
      case _ =>
    }
  }

  def validate(rdf: ReadDataFrame): Unit = {
    import InputStorageTypeChoice._

    rdf.getStorageType() match {
      case file: File =>
        val path = file.getSourceFile()
        FilePath(path)
      case _ =>
    }
  }

}
