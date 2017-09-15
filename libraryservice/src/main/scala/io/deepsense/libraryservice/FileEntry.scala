/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import spray.json.DefaultJsonProtocol

case class FileEntry(name: String, uri: String)

object FileEntry {
  def apply(name: String): FileEntry = new FileEntry(name, uriForName(name))

  private val UriPrefix = "file://"

  private def uriForName(name: String): String = {
    UriPrefix + Config.Storage.directory + name
  }
}

object FileEntryJsonProtocol extends DefaultJsonProtocol {
  implicit val fileEntryJsonFormat = jsonFormat2(FileEntry.apply)
}
