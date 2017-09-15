/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperations.readwritedataframe

import io.deepsense.deeplang.doperations.{WriteDataFrame, ReadDataFrame}
import io.deepsense.deeplang.doperations.inout.{OutputStorageTypeChoice, OutputFileFormatChoice, InputFileFormatChoice, InputStorageTypeChoice}

object ParquetSupportedOnClusterOnly {

  def validate(wdf: WriteDataFrame): Unit = {
    import OutputFileFormatChoice._
    import OutputStorageTypeChoice._

    wdf.getStorageType() match {
      case file: File =>
        file.getFileFormat() match {
          case _: Parquet =>
            val path = file.getOutputFile()
            val filePath = FilePath(path)
            val fileScheme = filePath.fileScheme
            if (fileScheme == FileScheme.File) {
              throw ParquetNotSupported
            }
          case _ =>
        }
      case _ =>
    }
  }

  def validate(rdf: ReadDataFrame): Unit = {
    import InputFileFormatChoice._
    import InputStorageTypeChoice._

    rdf.getStorageType() match {
      case file: File =>
        file.getFileFormat() match {
          case _: Parquet =>
            val path = file.getSourceFile()
            val filePath = FilePath(path)
            val fileScheme = filePath.fileScheme
            if (fileScheme == FileScheme.File) {
              throw ParquetNotSupported
            }
          case _ =>
        }
      case _ =>
    }
  }

}
