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
package ai.deepsense.deeplang.doperations.readwritedataframe

import ai.deepsense.deeplang.ExecutionContext

object LibrarySupport {

  def filePathFromLibraryPath(path: FilePath)(implicit context: ExecutionContext): FilePath = {
    require(path.fileScheme == FileScheme.Library)
    val libraryPath = context.libraryPath + "/" + path.pathWithoutScheme
    FilePath(FileScheme.File, libraryPath)
  }
  def hdfsPathFromLibraryPath(path: FilePath)(implicit context: ExecutionContext): Option[FilePath] = {
    require(path.fileScheme == FileScheme.Library)
    val libraryPath = context.libraryPath + "/" + path.pathWithoutScheme

    if (context.tempPath.startsWith("hdfs://") && !context.sparkContext.isLocal) {
      Some(FilePath(FileScheme.HDFS, libraryPath))
    } else {
      None
    }
  }
}
