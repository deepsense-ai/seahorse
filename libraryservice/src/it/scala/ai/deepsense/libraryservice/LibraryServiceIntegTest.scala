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
import java.nio.file.Files

import org.scalatest.{BeforeAndAfterEach, WordSpecLike}
import org.scalatra.servlet.MultipartConfig
import org.scalatra.test.scalatest._
import spray.json._

import ai.deepsense.libraryservice.FileEntryJsonProtocol._
import ai.deepsense.libraryservice.FileEntry._
import ai.deepsense.libraryservice.FileOperations._

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
  private val folderName = "folder"
  private val testStorageDirNewFolder = new File(testStorageDir.getPath + "/" + folderName)
  private val resourceFilesDir = new File(getClass.getResource("/files/").getPath)
  private val apiPrefix = Config.Server.apiPrefix

  private val testFile = new File(resourceFilesDir, "test_file.csv")
  private val testFile1 = new File(resourceFilesDir, "test_file1.csv")

  override def beforeEach(): Unit = {
    if (testStorageDir.exists) {
      deleteRecursively(testStorageDir)
    }
    if(testStorageDirNewFolder.exists) {
      deleteRecursively(testStorageDirNewFolder)
    }
    testStorageDir.mkdir()
    testStorageDirNewFolder.mkdir()
  }

  private def copyTestFileToStorageDir(dstName: String): Unit = {
    Files.copy(testFile.toPath, new File(testStorageDir, dstName).toPath)
  }

  private def copyTestFileToStorageDirNewFolder(dstName: String): Unit = {
    Files.copy(testFile.toPath, new File(testStorageDirNewFolder, dstName).toPath)
  }

  private def assertFilesEqual(file1: File, file2: File): Unit = {
    val bytes1 = Files.readAllBytes(file1.toPath)
    val bytes2 = Files.readAllBytes(file2.toPath)
    bytes1 should contain theSameElementsInOrderAs bytes2
  }

  private def assertResponseFileEquality(file: File, responseBody: String): Unit = {
    val content = Files.readAllLines(file.toPath).toArray
    val responseLines = responseBody.split("\n")
    responseLines should be(content)
  }

  "LibraryService upload" should {
    "save file to storage" in {
      post(apiPrefix, Map.empty, Map("file" -> testFile)) {
        status should equal(200)
        val uploadedFile = new File(testStorageDir, testFile.getName)
        assertFilesEqual(uploadedFile, testFile)
      }
    }
    "save file to existing directory" in {
      post(apiPrefix + "/" + folderName, Map.empty, Map("file" -> testFile)) {
        status should equal(200)
        val uploadedFile = new File(testStorageDir + "/" + folderName, testFile.getName)
        assertFilesEqual(uploadedFile, testFile)
      }
    }
    "save file to a directory that does not exist yet" in {
      val newDirectoryPath = "/a/new/directory/"
      post(apiPrefix + newDirectoryPath, Map.empty, Map("file" -> testFile)) {
        status should equal(200)
        val uploadedFile = new File(testStorageDir + newDirectoryPath, testFile.getName)
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
        assertResponseFileEquality(file, body)
      }
    }
    "return file from a directory" in {
      val file = testFile
      copyTestFileToStorageDirNewFolder(testFile.getName)
      get(s"$apiPrefix/$folderName/${testFile.getName}") {
        status should equal(200)
        assertResponseFileEquality(file, body)
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
    "return tree of files sorted by directory/file status and name" in {
      val names = Seq("xyz", "abc", "def")
      names.foreach(name => copyTestFileToStorageDir(dstName = name))
      get(s"$apiPrefix") {
        status should equal(200)
        val expectedFiles = fileToFileEntry(testStorageDir)
        val files = body.parseJson.convertTo[FileEntry]
        files should be(expectedFiles)
      }
    }
    "return a similar tree with a subdirectory as its root" in {
      get(s"$apiPrefix/$folderName") {
        status should equal(200)
        val expectedFileEntry = fileToFileEntry(testStorageDirNewFolder)
        body.parseJson.convertTo[FileEntry] should be(expectedFileEntry)
      }
    }
  }

  "LibraryService delete" should {
    "delete file and return success" in {
      copyTestFileToStorageDir(testFile.getName)
      delete(s"$apiPrefix/${testFile.getName}") {
        status should equal(200)
        new File(testStorageDir, testFile.getName).exists() shouldBe false
      }
    }
    "delete a non-empty directory and return success" in {
      copyTestFileToStorageDirNewFolder(testFile.getName)
      delete(s"$apiPrefix/$folderName") {
        status should equal(200)
        testStorageDirNewFolder.exists() shouldBe false
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
