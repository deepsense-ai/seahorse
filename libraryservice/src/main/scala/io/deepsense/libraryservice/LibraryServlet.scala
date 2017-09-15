/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import java.io._

import io.deepsense.libraryservice.FileEntryJsonProtocol._
import org.scalatra._
import org.scalatra.servlet.{FileUploadSupport, SizeConstraintExceededException}
import spray.json._

class LibraryServlet extends ScalatraServlet with FileUploadSupport with CorsSupport {

  private val libraryDir = new File(Config.Storage.directory)

  private val ApiPrefix = Config.Server.apiPrefix

  error {
    case e: SizeConstraintExceededException =>
      RequestEntityTooLarge("Size of the uploaded file exceeds the limit")
  }

  options("/*"){
    response.setHeader(
      "Access-Control-Allow-Headers",
      request.getHeader("Access-Control-Request-Headers"))
  }

  // This is for easy manual testing
  get("/upload") {
      <html>
        <body>
        <form action={url("/library")} method="post" enctype="multipart/form-data">
          <p>File to upload: <input type="file" name="file" /></p>
          <p><input type="submit" value="Upload" /></p>
        </form>
        </body>
      </html>
  }

  post(ApiPrefix) {
    fileParams.get("file") match {
      case Some(uploadedFile) =>
        val name = uploadedFile.name
        val file = getFile(name)
        uploadedFile.write(file)
      case None => BadRequest("No file provided")
    }
  }

  get(ApiPrefix) {
    val files = libraryDir.listFiles(new FileFilter{ def accept(f: File) = f.isFile })
    val sortedFiles = files.sortBy(f => f.getName)
    val names = sortedFiles.map(_.getName)
    val fileEntries = names.map(FileEntry.apply)
    Ok(fileEntries.toJson)
  }

  get(s"$ApiPrefix/:name") {
    val name = params("name")
    getProperFile(name) match {
      case Some(file) => Ok(file)
      case None => fileNotFound(name)
    }
  }

  delete(s"$ApiPrefix/:name") {
    val name = params("name")
    getProperFile(name) match {
      case Some(file) =>
        file.delete()
        Ok(s"File '$name' has been deleted")
      case None =>
        fileNotFound(name)
    }
  }

  private def getFile(name: String): File = new File(libraryDir, name)

  private def getProperFile(name: String): Option[File] = {
    val file = getFile(name)
    if (file.exists && file.isFile) {
      Some(file)
    } else {
      None
    }
  }

  private def fileNotFound(name: String) = NotFound(s"File with name '$name' not found")
}
