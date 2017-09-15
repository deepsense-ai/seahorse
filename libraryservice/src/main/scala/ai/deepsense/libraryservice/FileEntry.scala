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

package ai.deepsense.libraryservice

import java.io.File

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}
import ai.deepsense.commons.json.EnumerationSerializer._

case class FileEntry private[libraryservice] ( name: String,
                                               kind: FileType.Value,
                                               children: Seq[FileEntry])

object FileEntry {
  def fromFile(file: java.io.File): FileEntry = {
    FileEntry(file.getName, FileType.File, Nil)
  }

  def fromDirectory(directory: java.io.File): FileEntry = {
    val sortedFiles = directory.listFiles.sortBy(f => (f.isFile, f.getName))
    val children = sortedFiles.map(fileToFileEntry)
    FileEntry(directory.getName, FileType.Directory, children)
  }

  def fileToFileEntry(f: File): FileEntry = {
    if (f.isFile) {
      fromFile(f)
    } else {
      fromDirectory(f)
    }
  }
}

object FileType extends Enumeration {
  val File = Value("file")
  val Directory = Value("directory")

  implicit val fileTypeJsonFormat = jsonEnumFormat(FileType)
}

object FileEntryJsonProtocol extends DefaultJsonProtocol {
  implicit val fileEntryJsonFormat: JsonFormat[FileEntry] =
    lazyFormat(jsonFormat3(FileEntry.apply))
}
