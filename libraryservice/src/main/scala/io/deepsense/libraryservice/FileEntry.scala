/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import java.io.File

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}
import io.deepsense.commons.json.EnumerationSerializer._

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
