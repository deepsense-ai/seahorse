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
package ai.deepsense.libraryservice

import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.Files.copy
import java.nio.file.Paths.get

import ai.deepsense.commons.utils.LoggerForCallerClass

object DefaultLibraryFiles {
  implicit def toPath (filename: String) = get(filename)
  val libraryDefaultPath = Config.Storage.directory
  val logger = LoggerForCallerClass()

  def copyDefaultLibraryFiles() = {
    try {
      copyLibraryFile("episodes.avro")
    } catch {
      case ex: java.nio.file.NoSuchFileException => logger.warn(s"Failed to copy file.", ex.getCause)
    }
  }

  def copyLibraryFile(filename: String) = {
    val src = s"/opt/docker/app/$filename"
    val dest = s"$libraryDefaultPath/$filename"
    logger.info(s"Copy $src to $dest")
    copy(src, dest, REPLACE_EXISTING)
  }
}
