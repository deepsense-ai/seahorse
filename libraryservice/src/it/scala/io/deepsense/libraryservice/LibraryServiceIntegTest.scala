/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import java.io.File
import java.nio.file.Files

import org.scalatest.{BeforeAndAfterEach, WordSpecLike}
import org.scalatra.servlet.MultipartConfig
import org.scalatra.test.scalatest._
import spray.json._

import io.deepsense.libraryservice.FileEntryJsonProtocol._

class LibraryServiceIntegTest
  extends ScalatraSuite
  with WordSpecLike
  with BeforeAndAfterEach {

  val holder = addServlet(classOf[LibraryServlet], "/*")
    holder.getRegistration.setMultipartConfig(MultipartConfig(
      maxFileSize = Some(3*1024*1024),
      fileSizeThreshold = Some(1*1024*1024)
    ).toMultipartConfigElement)

  private val testStorageDir = new File(Config.Storage.directory)
  private val resourceFilesDir = new File(getClass.getResource("/files/").getPath)
  private val apiPrefix = Config.Server.apiPrefix

  private val testFile = new File(resourceFilesDir, "test_file.csv")
  private val testFile1 = new File(resourceFilesDir, "test_file1.csv")

  override def beforeEach(): Unit = {
    if (testStorageDir.exists) {
      testStorageDir.listFiles.foreach(_.delete())
    } else {
      testStorageDir.mkdir()
    }
  }

  private def copyTestFileToStorageDir(dstName: String): Unit = {
    Files.copy(testFile.toPath, new File(testStorageDir, dstName).toPath)
  }

  private def assertFilesEqual(file1: File, file2: File): Unit = {
    val bytes1 = Files.readAllBytes(file1.toPath)
    val bytes2 = Files.readAllBytes(file2.toPath)
    bytes1 should contain theSameElementsInOrderAs bytes2
  }

  "LibraryService upload" should {
    "save file to storage" in {
      post(apiPrefix, Map.empty, Map("file" -> testFile)) {
        status should equal(200)
        val uploadedFile = new File(testStorageDir, testFile.getName)
        assertFilesEqual(uploadedFile, testFile)
      }
    }
    "overwrite file" when {
      "filename already exists" in {
        copyTestFileToStorageDir(dstName = testFile1.getName)
        post(apiPrefix, Map.empty, Map("file" -> testFile1)) {
          status should equal(200)
          val uploadedFile = new File(testStorageDir, testFile1.getName)
          assertFilesEqual(uploadedFile, testFile1)
        }
      }
    }
  }

  "LibraryService download" should {
    "return file" in {
      val file = testFile
      copyTestFileToStorageDir(testFile.getName)
      get(s"$apiPrefix/${testFile.getName}") {
        status should equal(200)
        val content = Files.readAllLines(file.toPath).toArray
        val responseLines = body.split("\n")
        responseLines should be(content)
      }
    }
    "return Not Found" when {
      "file does not exist" in {
        get(s"$apiPrefix/${testFile.getName}") {
          status should equal(404)
        }
      }
    }
  }

  "LibraryService list" should {
    "return list of files sorted by name" in {
      val names = Seq("xyz", "abc", "def")
      names.foreach(name => copyTestFileToStorageDir(dstName = name))
      get(s"$apiPrefix") {
        status should equal(200)
        val expectedNames = names.sorted
        val expectedFileEntries = expectedNames.map(FileEntry.apply)
        body.parseJson.convertTo[List[FileEntry]] should be(expectedFileEntries)
      }
    }
  }

  "LibraryService delete" should {
    "delete file and return success" in {
      val file = testFile
      copyTestFileToStorageDir(testFile.getName)
      delete(s"$apiPrefix/${testFile.getName}") {
        status should equal(200)
        new File(testStorageDir, testFile.getName).exists() shouldBe false
      }
    }
    "return Not Found" when {
      "file does not exist" in {
        delete(s"$apiPrefix/${testFile.getName}") {
          status should equal(404)
        }
      }
    }
  }
}
