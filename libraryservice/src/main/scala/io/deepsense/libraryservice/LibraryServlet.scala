/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import java.io._

import io.deepsense.libraryservice.FileEntryJsonProtocol._
import io.deepsense.libraryservice.FileOperations._
import io.deepsense.libraryservice.FileEntry._
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

  options("/*") {
    response.setHeader(
      "Access-Control-Allow-Headers",
      request.getHeader("Access-Control-Request-Headers"))
  }

  // This is for easy manual testing
  get("/upload") {
    <html>
      <body>
        <form action={url("/library")} method="post" enctype="multipart/form-data">
          <p>File to upload:
            <input type="file" name="file"/>
          </p>
          <p>
            <input type="submit" value="Upload"/>
          </p>
        </form>
      </body>
    </html>
  }

  post(ApiPrefix) {
    fileParams.get("file") match {
      case Some(uploadedFile) =>
        val name = uploadedFile.name
        val file = new File(libraryDir, name)
        uploadedFile.write(file)
      case None => BadRequest("No file sent")
    }
  }

  post(s"$ApiPrefix/*") {
    val path = getPathFromParams
    new File(path).mkdirs()
    fileParams.get("file") match {
      case Some(uploadedFile) =>
        val name = uploadedFile.name
        val file = new File(path, name)
        uploadedFile.write(file)
      case None => Ok(s"Directories on path $path have been created")
    }
  }

  get(ApiPrefix) {
    Ok(fileToFileEntry(libraryDir).toJson)
  }

  get(s"$ApiPrefix/*") {
    val path = getPathFromParams
    getProperFile(path) match {
      case Some(file) => Ok(
        if (file.isFile) {
          file
        } else {
          fileToFileEntry(file).toJson
        })
      case None => fileNotFound(path)
    }
  }

  delete(s"$ApiPrefix/*") {
    val path = getPathFromParams
    getProperFile(path) match {
      case Some(file) =>
        deleteRecursively(file)
        Ok(s"'$path' has been deleted")
      case None =>
        fileNotFound(path)
    }
  }

  private def getProperFile(path: String): Option[File] = {
    val file = new File(path)
    if (file.exists) {
      Some(file)
    } else {
      None
    }
  }

  private def fileNotFound(name: String) = NotFound(s"File with name '$name' not found")

  private def getPathFromParams = libraryDir + "/" + params("splat")
}
