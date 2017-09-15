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

object FilePathFromLibraryPath {

  def apply(path: FilePath): FilePath = {
    require(path.fileScheme == FileScheme.Library)
    // TODO There might be need to have it passed to we.jar as argument eventually
    val filePathForLibrary = "/library/"
    val libraryPath = filePathForLibrary + path.pathWithoutScheme
    FilePath(FileScheme.File, libraryPath)
  }

}
